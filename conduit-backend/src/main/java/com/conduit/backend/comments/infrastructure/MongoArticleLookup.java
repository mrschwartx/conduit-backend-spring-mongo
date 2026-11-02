package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.ArticleLookup;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.util.Optional;
import java.util.UUID;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class MongoArticleLookup implements ArticleLookup {

	private final MongoCollection<Document> articles;

	public MongoArticleLookup(MongoDatabase database) {
		this.articles = database.getCollection(MongoCollections.ARTICLES);
	}

	@Override
	public Optional<UUID> findIdBySlug(String slug) {
		Document article = articles.find(Filters.eq("slug", slug)).projection(Projections.include("_id")).first();
		return Optional.ofNullable(article).map(document -> BsonValues.uuid(document, "_id"));
	}
}
