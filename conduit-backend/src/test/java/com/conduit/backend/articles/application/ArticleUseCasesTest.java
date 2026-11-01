package com.conduit.backend.articles.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.shared.domain.ForbiddenException;
import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.ValidationException;
import com.conduit.backend.support.InMemoryArticleStore;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArticleUseCasesTest {

	private InMemoryArticleStore store;

	private CreateArticle createArticle;

	private UpdateArticle updateArticle;

	private DeleteArticle deleteArticle;

	private final UUID author = UUID.randomUUID();

	private final UUID stranger = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		store = new InMemoryArticleStore();
		UniqueSlugs slugs = new UniqueSlugs(store);
		createArticle = new CreateArticle(store, store, slugs);
		updateArticle = new UpdateArticle(store, store, slugs);
		deleteArticle = new DeleteArticle(store);
	}

	private ArticleDto create(String title) {
		return createArticle.execute(author, Map.of("title", title, "description", "About it", "body", "Details",
				"tagList", List.of("beta", "alpha")));
	}

	@Test
	void createsAnArticleWithSlugAndTagsInGivenOrder() {
		ArticleDto article = create("Hello World");

		assertThat(article.slug()).isEqualTo("hello-world");
		assertThat(article.tagList()).containsExactly("beta", "alpha");
		assertThat(article.favorited()).isFalse();
		assertThat(article.favoritesCount()).isZero();
	}

	@Test
	void createRequiresTitleDescriptionAndBody() {
		assertThatThrownBy(() -> createArticle.execute(author, Map.of("description", "d", "body", "b")))
				.isInstanceOf(ValidationException.class).extracting("errors")
				.isEqualTo(Map.of("title", List.of("can't be blank")));
		assertThatThrownBy(() -> createArticle.execute(author, Map.of("title", "t", "body", "b")))
				.isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> createArticle.execute(author, Map.of("title", "t", "description", "d")))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void createReportsEveryInvalidField() {
		assertThatThrownBy(() -> createArticle.execute(author, Map.of()))
				.isInstanceOf(ValidationException.class)
				.extracting("errors").asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
				.containsOnlyKeys("title", "description", "body");
	}

	@Test
	void duplicateTitlesGetNumericSuffixes() {
		assertThat(create("Hello World").slug()).isEqualTo("hello-world");
		assertThat(create("hello   world").slug()).isEqualTo("hello-world-2");
		assertThat(create("Hello World!").slug()).isEqualTo("hello-world-3");
	}

	@Test
	void theReservedFeedSlugIsNeverUsed() {
		assertThat(create("Feed").slug()).isEqualTo("feed-2");
	}

	@Test
	void authorUpdatesAnArticleAndTheSlugFollowsTheTitle() {
		create("Hello World");

		ArticleDto updated = updateArticle.execute(author, "hello-world", Map.of("title", "Goodbye World",
				"tagList", List.of("gamma")));

		assertThat(updated.slug()).isEqualTo("goodbye-world");
		assertThat(updated.body()).isEqualTo("Details");
		assertThat(updated.tagList()).containsExactly("gamma");
	}

	@Test
	void updateKeepsTheSlugWhenTheTitleIsUnchangedAndSuffixesCollisions() {
		create("Hello World");
		create("Other Article");

		ArticleDto same = updateArticle.execute(author, "hello-world", Map.of("title", "Hello World"));
		ArticleDto collided = updateArticle.execute(author, "hello-world", Map.of("title", "Other Article"));

		assertThat(same.slug()).isEqualTo("hello-world");
		assertThat(collided.slug()).isEqualTo("other-article-2");
	}

	@Test
	void updateRejectsStrangersMissingArticlesEmptyChangesAndTitleCollisions() {
		create("Hello World");
		create("Other Article");

		assertThatThrownBy(() -> updateArticle.execute(stranger, "hello-world", Map.of("body", "x")))
				.isInstanceOf(ForbiddenException.class)
				.extracting("errors").isEqualTo(Map.of("article", List.of("forbidden")));
		assertThatThrownBy(() -> updateArticle.execute(author, "missing", Map.of("body", "x")))
				.isInstanceOf(NotFoundException.class)
				.extracting("errors").isEqualTo(Map.of("article", List.of("not found")));
		assertThatThrownBy(() -> updateArticle.execute(author, "hello-world", Map.of()))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void deleteIsRestrictedToTheAuthor() {
		create("Hello World");

		assertThatThrownBy(() -> deleteArticle.execute(stranger, "hello-world"))
				.isInstanceOf(ForbiddenException.class);
		deleteArticle.execute(author, "hello-world");
		assertThatThrownBy(() -> deleteArticle.execute(author, "hello-world"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void listsDistinctTags() {
		create("Hello World");
		create("Second");

		assertThat(new ListTags(store).execute()).containsExactly("alpha", "beta");
	}
}
