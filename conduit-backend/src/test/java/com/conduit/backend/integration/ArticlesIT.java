package com.conduit.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ArticlesIT extends IntegrationTestSupport {

	private String create(Registered author, String title, String... tags) {
		ApiResponse response = author.client().post("/api/v1/articles", articleBody(title, tags));
		assertThat(response.status()).isEqualTo(201);
		return response.json("$.article.slug");
	}

	@Test
	void createsAnArticleWithAuthorAndTags() {
		Registered author = register();
		String title = uniqueTitle("Hello World");

		ApiResponse response = author.client().post("/api/v1/articles", articleBody(title, "zeta", "alpha", "zeta"));

		assertThat(response.status()).isEqualTo(201);
		assertThat((String) response.json("$.article.slug")).isEqualTo(slugOf(title));
		assertThat((String) response.json("$.article.title")).isEqualTo(title);
		assertThat((String) response.json("$.article.body")).isEqualTo("The body");
		assertThat((List<String>) response.json("$.article.tagList")).containsExactly("zeta", "alpha");
		assertThat(response.<Boolean>json("$.article.favorited")).isFalse();
		assertThat(response.<Integer>json("$.article.favoritesCount")).isZero();
		assertThat((String) response.json("$.article.author.username")).isEqualTo(author.username());
		assertThat(response.<Boolean>json("$.article.author.following")).isFalse();
		assertThat((String) response.json("$.article.createdAt")).endsWith("Z");
	}

	@Test
	void createRejectsInvalidArticlesAndReportsEveryField() {
		Registered author = register();
		ApiResponse blank = author.client().post("/api/v1/articles",
				"{\"article\":{\"title\":\"\",\"description\":\"d\",\"body\":\"b\"}}");
		ApiResponse missingBody = author.client().post("/api/v1/articles",
				"{\"article\":{\"title\":\"t\",\"description\":\"d\"}}");
		ApiResponse badTags = author.client().post("/api/v1/articles",
				"{\"article\":{\"title\":\"t\",\"description\":\"d\",\"body\":\"b\",\"tagList\":\"nope\"}}");

		ApiResponse everything = author.client().post("/api/v1/articles", "{\"article\":{}}");
		ApiResponse tooManyTags = author.client().post("/api/v1/articles", articleBody("t", "1", "2", "3", "4", "5",
				"6", "7", "8", "9", "10", "11"));
		ApiResponse spacedTag = author.client().post("/api/v1/articles", articleBody("t", "a b"));

		assertThat(everything.status()).isEqualTo(422);
		assertThat((List<String>) everything.json("$.errors.title")).containsExactly("can't be blank");
		assertThat((List<String>) everything.json("$.errors.description")).containsExactly("can't be blank");
		assertThat((List<String>) everything.json("$.errors.body")).containsExactly("can't be blank");
		assertThat(tooManyTags.status()).isEqualTo(422);
		assertThat(spacedTag.status()).isEqualTo(422);
		assertThat(blank.status()).isEqualTo(422);
		assertThat((List<String>) blank.json("$.errors.title")).containsExactly("can't be blank");
		assertThat(missingBody.status()).isEqualTo(422);
		assertThat(badTags.status()).isEqualTo(422);
	}

	@Test
	void duplicateTitlesGetNumericSuffixes() {
		Registered author = register();
		String title = uniqueTitle("Duplicate");
		String first = create(author, title);

		String second = create(author, title);
		String third = create(author, title);
		String feed = create(author, "Feed");

		assertThat(first).isEqualTo(slugOf(title));
		assertThat(second).isEqualTo(first + "-2");
		assertThat(third).isEqualTo(first + "-3");
		assertThat(feed).startsWith("feed-");
	}

	@Test
	void getsAnArticleAnonymouslyAndReportsMissingOnes() {
		Registered author = register();
		String slug = create(author, uniqueTitle("Readable"));

		ApiResponse found = client().get("/api/v1/articles/" + slug);
		ApiResponse missing = client().get("/api/v1/articles/does-not-exist");

		assertThat(found.status()).isEqualTo(200);
		assertThat((String) found.json("$.article.slug")).isEqualTo(slug);
		assertThat(missing.status()).isEqualTo(404);
		assertThat((List<String>) missing.json("$.errors.article")).containsExactly("not found");
	}

	@Test
	void updatesAnArticleAndRegeneratesTheSlug() {
		Registered author = register();
		String slug = create(author, uniqueTitle("Before"), "one");
		String newTitle = uniqueTitle("After");

		ApiResponse response = author.client().put("/api/v1/articles/" + slug,
				"{\"article\":{\"title\":\"" + newTitle + "\",\"tagList\":[\"two\"]}}");

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.article.slug")).isEqualTo(slugOf(newTitle));
		assertThat((String) response.json("$.article.body")).isEqualTo("The body");
		assertThat((List<String>) response.json("$.article.tagList")).containsExactly("two");
		assertThat(client().get("/api/v1/articles/" + slug).status()).isEqualTo(404);
	}

	@Test
	void updateAndDeleteAreRestrictedToTheAuthor() {
		Registered author = register();
		Registered stranger = register();
		String slug = create(author, uniqueTitle("Protected"));

		ApiResponse update = stranger.client().put("/api/v1/articles/" + slug, "{\"article\":{\"body\":\"hacked\"}}");
		ApiResponse delete = stranger.client().delete("/api/v1/articles/" + slug);
		ApiResponse empty = author.client().put("/api/v1/articles/" + slug, "{\"article\":{}}");
		ApiResponse missing = author.client().put("/api/v1/articles/nope", "{\"article\":{\"body\":\"x\"}}");

		assertThat(update.status()).isEqualTo(403);
		assertThat((List<String>) update.json("$.errors.article")).containsExactly("forbidden");
		assertThat(delete.status()).isEqualTo(403);
		assertThat(empty.status()).isEqualTo(422);
		assertThat(missing.status()).isEqualTo(404);
		assertThat((String) client().get("/api/v1/articles/" + slug).json("$.article.body")).isEqualTo("The body");
	}

	@Test
	void deletesAnArticleTogetherWithItsComments() {
		Registered author = register();
		String slug = create(author, uniqueTitle("Disposable"));
		author.client().post("/api/v1/articles/" + slug + "/comments", "{\"comment\":{\"body\":\"bye\"}}");

		ApiResponse response = author.client().delete("/api/v1/articles/" + slug);

		assertThat(response.status()).isEqualTo(204);
		assertThat(response.body()).isEmpty();
		assertThat(client().get("/api/v1/articles/" + slug).status()).isEqualTo(404);
		assertThat(client().get("/api/v1/articles/" + slug + "/comments").status()).isEqualTo(404);
	}

	@Test
	void listsArticlesNewestFirstWithPagination() {
		Registered author = register();
		String first = create(author, uniqueTitle("First"));
		String second = create(author, uniqueTitle("Second"));
		String third = create(author, uniqueTitle("Third"));
		String path = "/api/v1/articles?author=" + author.username();

		ApiResponse all = client().get(path);
		ApiResponse page = client().get(path + "&limit=1&offset=1");

		assertThat(all.status()).isEqualTo(200);
		assertThat(all.<Integer>json("$.articlesCount")).isEqualTo(3);
		assertThat((List<String>) all.json("$.articles[*].slug")).containsExactly(third, second, first);
		assertThat(client().get(path + "&limit=500").status()).isEqualTo(200);
		assertThat(all.body()).doesNotContain("\"body\"");
		assertThat(page.<Integer>json("$.articlesCount")).isEqualTo(3);
		assertThat((List<String>) page.json("$.articles[*].slug")).containsExactly(second);
	}

	@Test
	void filtersByTagAuthorAndFavorited() {
		Registered author = register();
		Registered fan = register();
		String tag = "tag" + System.nanoTime();
		String tagged = create(author, uniqueTitle("Tagged"), tag);
		create(author, uniqueTitle("Untagged"));
		fan.client().post("/api/v1/articles/" + tagged + "/favorite", "");

		ApiResponse byTag = client().get("/api/v1/articles?tag=" + tag);
		ApiResponse byFavorite = client().get("/api/v1/articles?favorited=" + fan.username());
		ApiResponse byUnknownAuthor = client().get("/api/v1/articles?author=nobody_here");

		assertThat((List<String>) byTag.json("$.articles[*].slug")).containsExactly(tagged);
		assertThat((List<String>) byFavorite.json("$.articles[*].slug")).containsExactly(tagged);
		assertThat(byUnknownAuthor.<Integer>json("$.articlesCount")).isZero();
		assertThat((List<String>) byUnknownAuthor.json("$.articles")).isEmpty();
	}

	@Test
	void rejectsInvalidPagination() {
		assertThat(client().get("/api/v1/articles?limit=0").status()).isEqualTo(422);
		assertThat(client().get("/api/v1/articles?offset=-1").status()).isEqualTo(422);
		assertThat(client().get("/api/v1/articles?limit=abc").status()).isEqualTo(422);
	}

	@Test
	void favoritesAndUnfavoritesAreIdempotent() {
		Registered author = register();
		Registered fan = register();
		String slug = create(author, uniqueTitle("Likeable"));

		ApiResponse first = fan.client().post("/api/v1/articles/" + slug + "/favorite", "");
		ApiResponse second = fan.client().post("/api/v1/articles/" + slug + "/favorite", "");
		ApiResponse anonymous = client().get("/api/v1/articles/" + slug);
		ApiResponse removed = fan.client().delete("/api/v1/articles/" + slug + "/favorite");
		ApiResponse missing = fan.client().post("/api/v1/articles/nope/favorite", "");

		assertThat(first.<Boolean>json("$.article.favorited")).isTrue();
		assertThat(first.<Integer>json("$.article.favoritesCount")).isEqualTo(1);
		assertThat(second.<Integer>json("$.article.favoritesCount")).isEqualTo(1);
		assertThat(anonymous.<Boolean>json("$.article.favorited")).isFalse();
		assertThat(anonymous.<Integer>json("$.article.favoritesCount")).isEqualTo(1);
		assertThat(removed.<Boolean>json("$.article.favorited")).isFalse();
		assertThat(removed.<Integer>json("$.article.favoritesCount")).isZero();
		assertThat(missing.status()).isEqualTo(404);
	}

	@Test
	void feedShowsOnlyFollowedAuthors() {
		Registered reader = register();
		Registered followed = register();
		Registered other = register();
		String wanted = create(followed, uniqueTitle("Wanted"));
		create(other, uniqueTitle("Ignored"));
		reader.client().post("/api/v1/profiles/" + followed.username() + "/follow", "");

		ApiResponse feed = reader.client().get("/api/v1/articles/feed");

		assertThat(feed.status()).isEqualTo(200);
		assertThat(feed.<Integer>json("$.articlesCount")).isEqualTo(1);
		assertThat((List<String>) feed.json("$.articles[*].slug")).containsExactly(wanted);
		assertThat(feed.<Boolean>json("$.articles[0].author.following")).isTrue();
	}

	@Test
	void listsDistinctTags() {
		Registered author = register();
		String tag = "unique" + System.nanoTime();
		create(author, uniqueTitle("One"), tag);
		create(author, uniqueTitle("Two"), tag);

		ApiResponse response = client().get("/api/v1/tags");

		assertThat(response.status()).isEqualTo(200);
		assertThat((List<String>) response.json("$.tags")).contains(tag).doesNotHaveDuplicates().hasSizeLessThanOrEqualTo(20);
	}

	@Test
	void addsListsAndDeletesComments() {
		Registered author = register();
		Registered commenter = register();
		String slug = create(author, uniqueTitle("Discussed"));
		String path = "/api/v1/articles/" + slug + "/comments";

		ApiResponse created = commenter.client().post(path, "{\"comment\":{\"body\":\"Nice one\"}}");
		ApiResponse listed = client().get(path);
		int id = created.json("$.comment.id");
		ApiResponse strangerDelete = author.client().delete(path + "/" + id);
		ApiResponse deleted = commenter.client().delete(path + "/" + id);
		ApiResponse after = client().get(path);

		assertThat(created.status()).isEqualTo(201);
		assertThat((String) created.json("$.comment.body")).isEqualTo("Nice one");
		assertThat((String) created.json("$.comment.author.username")).isEqualTo(commenter.username());
		assertThat(listed.status()).isEqualTo(200);
		assertThat((List<String>) listed.json("$.comments[*].body")).containsExactly("Nice one");
		assertThat(strangerDelete.status()).isEqualTo(403);
		assertThat((List<String>) strangerDelete.json("$.errors.comment")).containsExactly("forbidden");
		assertThat(deleted.status()).isEqualTo(204);
		assertThat((List<String>) after.json("$.comments")).isEmpty();
	}

	@Test
	void listsCommentsNewestFirst() {
		Registered author = register();
		String slug = create(author, uniqueTitle("Ordered"));
		String path = "/api/v1/articles/" + slug + "/comments";
		author.client().post(path, "{\"comment\":{\"body\":\"older\"}}");
		author.client().post(path, "{\"comment\":{\"body\":\"newer\"}}");

		ApiResponse response = client().get(path);

		assertThat((List<String>) response.json("$.comments[*].body")).containsExactly("newer", "older");
	}

	@Test
	void commentErrorsAreReported() {
		Registered author = register();
		String slug = create(author, uniqueTitle("Quiet"));
		String path = "/api/v1/articles/" + slug + "/comments";

		ApiResponse blank = author.client().post(path, "{\"comment\":{\"body\":\"  \"}}");
		ApiResponse noArticle = author.client().post("/api/v1/articles/nope/comments", "{\"comment\":{\"body\":\"x\"}}");
		ApiResponse noComment = author.client().delete(path + "/999999");
		ApiResponse badId = author.client().delete(path + "/abc");
		ApiResponse anonymousPost = client().post(path, "{\"comment\":{\"body\":\"x\"}}");

		assertThat(blank.status()).isEqualTo(422);
		assertThat((List<String>) blank.json("$.errors.body")).containsExactly("can't be blank");
		assertThat(noArticle.status()).isEqualTo(404);
		assertThat(noComment.status()).isEqualTo(404);
		assertThat((List<String>) noComment.json("$.errors.comment")).containsExactly("not found");
		assertThat(badId.status()).isEqualTo(404);
		assertThat(anonymousPost.status()).isEqualTo(401);
	}
}
