package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleFilter;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.articles.domain.ArticleView;
import com.conduit.backend.articles.domain.ArticleViewPage;
import com.conduit.backend.shared.domain.PageWindow;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.infrastructure.persistence.AuthorProfiles;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

@Repository
public class MongoArticleQueries implements ArticleQueries {

	private static final int TAG_LIMIT = 20;

	private static final Bson NEWEST_FIRST = Sorts.orderBy(Sorts.descending("createdAt"), Sorts.descending("_id"));

	private final MongoCollection<Document> articles;

	private final MongoCollection<Document> users;

	private final MongoCollection<Document> follows;

	private final MongoCollection<Document> favorites;

	private final AuthorProfiles authorProfiles;

	public MongoArticleQueries(MongoDatabase database, AuthorProfiles authorProfiles) {
		this.articles = database.getCollection(MongoCollections.ARTICLES);
		this.users = database.getCollection(MongoCollections.USERS);
		this.follows = database.getCollection(MongoCollections.FOLLOWS);
		this.favorites = database.getCollection(MongoCollections.FAVORITES);
		this.authorProfiles = authorProfiles;
	}

	@Override
	public Optional<ArticleView> findBySlug(String slug, UUID viewerId) {
		Document document = articles.find(Filters.eq("slug", slug)).first();
		if (document == null) {
			return Optional.empty();
		}
		return hydrate(List.of(document), viewerId).stream().findFirst();
	}

	@Override
	public ArticleViewPage search(ArticleFilter filter, UUID viewerId) {
		List<Bson> conditions = new ArrayList<>();
		if (filter.tag() != null) {
			conditions.add(Filters.eq("tags", filter.tag()));
		}
		if (filter.author() != null) {
			Optional<String> authorId = userIdOf(filter.author());
			if (authorId.isEmpty()) {
				return emptyPage();
			}
			conditions.add(Filters.eq("authorId", authorId.get()));
		}
		if (filter.favoritedBy() != null) {
			Optional<String> userId = userIdOf(filter.favoritedBy());
			if (userId.isEmpty()) {
				return emptyPage();
			}
			conditions.add(Filters.in("_id", favoritedArticleIds(userId.get())));
		}
		Bson query = conditions.isEmpty() ? new Document() : Filters.and(conditions);
		return page(query, filter.window(), viewerId);
	}

	@Override
	public ArticleViewPage feed(UUID viewerId, PageWindow window) {
		List<String> followeeIds = followeeIds(viewerId);
		if (followeeIds.isEmpty()) {
			return emptyPage();
		}
		return page(Filters.in("authorId", followeeIds), window, viewerId);
	}

	@Override
	public List<String> allTags() {
		List<Document> rows = articles.aggregate(List.of(
				Aggregates.unwind("$tags"),
				Aggregates.group("$tags", Accumulators.sum("count", 1)),
				Aggregates.sort(Sorts.orderBy(Sorts.descending("count"), Sorts.ascending("_id"))),
				Aggregates.limit(TAG_LIMIT))).into(new ArrayList<>());
		return rows.stream().map(row -> row.getString("_id")).toList();
	}

	private ArticleViewPage emptyPage() {
		return new ArticleViewPage(List.of(), 0);
	}

	private Optional<String> userIdOf(String username) {
		Document user = users.find(Filters.eq("usernameLower", BsonValues.lower(username)))
				.projection(Projections.include("_id")).first();
		return Optional.ofNullable(user).map(document -> document.getString("_id"));
	}

	private List<String> favoritedArticleIds(String userId) {
		List<String> ids = new ArrayList<>();
		for (Document row : favorites.find(Filters.eq("userId", userId))
				.projection(Projections.include("articleId"))) {
			ids.add(row.getString("articleId"));
		}
		return ids;
	}

	private List<String> followeeIds(UUID followerId) {
		List<String> ids = new ArrayList<>();
		for (Document row : follows.find(Filters.eq("followerId", BsonValues.id(followerId)))
				.projection(Projections.include("followeeId"))) {
			ids.add(row.getString("followeeId"));
		}
		return ids;
	}

	private ArticleViewPage page(Bson query, PageWindow window, UUID viewerId) {
		long total = articles.countDocuments(query);
		List<Document> documents = articles.find(query)
				.sort(NEWEST_FIRST)
				.skip(window.offset())
				.limit(window.limit())
				.into(new ArrayList<>());
		return new ArticleViewPage(hydrate(documents, viewerId), total);
	}

	private List<ArticleView> hydrate(List<Document> documents, UUID viewerId) {
		if (documents.isEmpty()) {
			return List.of();
		}
		List<Article> domain = documents.stream().map(ArticleDocuments::toDomain).toList();
		List<String> ids = domain.stream().map(article -> BsonValues.id(article.getId())).toList();
		Map<String, Long> counts = favoriteCounts(ids);
		Set<String> favorited = viewerId == null ? Set.of() : favoritedBy(ids, viewerId);
		Set<UUID> authorIds = new HashSet<>();
		domain.forEach(article -> authorIds.add(article.getAuthorId()));
		Map<UUID, Profile> authors = authorProfiles.load(authorIds, viewerId);

		List<ArticleView> views = new ArrayList<>();
		for (Article article : domain) {
			String id = BsonValues.id(article.getId());
			views.add(new ArticleView(article, authors.get(article.getAuthorId()), favorited.contains(id),
					counts.getOrDefault(id, 0L)));
		}
		return views;
	}

	private Map<String, Long> favoriteCounts(List<String> articleIds) {
		Map<String, Long> counts = new HashMap<>();
		List<Document> rows = favorites.aggregate(List.of(
				Aggregates.match(Filters.in("articleId", articleIds)),
				Aggregates.group("$articleId", Accumulators.sum("count", 1)))).into(new ArrayList<>());
		for (Document row : rows) {
			counts.put(row.getString("_id"), ((Number) row.get("count")).longValue());
		}
		return counts;
	}

	private Set<String> favoritedBy(List<String> articleIds, UUID viewerId) {
		Set<String> favorited = new HashSet<>();
		for (Document row : favorites.find(Filters.and(
				Filters.eq("userId", BsonValues.id(viewerId)),
				Filters.in("articleId", articleIds))).projection(Projections.include("articleId"))) {
			favorited.add(row.getString("articleId"));
		}
		return favorited;
	}
}
