package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.DuplicateKeys;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.Optional;
import java.util.UUID;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

@Repository
public class MongoArticleRepository implements ArticleRepository {

	private final MongoCollection<Document> articles;

	private final MongoCollection<Document> favorites;

	private final MongoCollection<Document> comments;

	public MongoArticleRepository(MongoDatabase database) {
		this.articles = database.getCollection(MongoCollections.ARTICLES);
		this.favorites = database.getCollection(MongoCollections.FAVORITES);
		this.comments = database.getCollection(MongoCollections.COMMENTS);
	}

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	public void add(Article article) {
		try {
			articles.insertOne(ArticleDocuments.from(article));
		}
		catch (MongoException error) {
			throw translate(error);
		}
	}

	@Override
	public boolean update(Article article) {
		try {
			return articles.updateOne(Filters.eq("_id", BsonValues.id(article.getId())), Updates.combine(
					Updates.set("slug", article.getSlug().value()),
					Updates.set("title", article.getTitle().value()),
					Updates.set("description", article.getDescription().value()),
					Updates.set("body", article.getBody().value()),
					Updates.set("tags", article.getTags().values()),
					Updates.set("updatedAt", BsonValues.date(article.getUpdatedAt())))).getMatchedCount() == 1;
		}
		catch (MongoException error) {
			throw translate(error);
		}
	}

	@Override
	public Optional<Article> findBySlug(String slug) {
		return Optional.ofNullable(articles.find(Filters.eq("slug", slug)).first())
				.map(ArticleDocuments::toDomain);
	}

	@Override
	public boolean existsBySlug(String slug, UUID excludeId) {
		Bson filter = excludeId == null
				? Filters.eq("slug", slug)
				: Filters.and(Filters.eq("slug", slug), Filters.ne("_id", BsonValues.id(excludeId)));
		return articles.countDocuments(filter) > 0;
	}

	@Override
	public boolean deleteById(UUID id) {
		String articleId = BsonValues.id(id);
		if (articles.deleteOne(Filters.eq("_id", articleId)).getDeletedCount() != 1) {
			return false;
		}
		favorites.deleteMany(Filters.eq("articleId", articleId));
		comments.deleteMany(Filters.eq("articleId", articleId));
		return true;
	}

	private static RuntimeException translate(MongoException error) {
		if (DuplicateKeys.isDuplicate(error)) {
			return new AlreadyExistsException("title");
		}
		return error;
	}
}
