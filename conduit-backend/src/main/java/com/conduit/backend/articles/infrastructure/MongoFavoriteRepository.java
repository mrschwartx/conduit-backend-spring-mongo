package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.FavoriteRepository;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.DuplicateKeys;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import java.util.Date;
import java.util.UUID;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

@Repository
public class MongoFavoriteRepository implements FavoriteRepository {

	private final MongoCollection<Document> favorites;

	public MongoFavoriteRepository(MongoDatabase database) {
		this.favorites = database.getCollection(MongoCollections.FAVORITES);
	}

	@Override
	public void favorite(UUID userId, UUID articleId) {
		try {
			favorites.updateOne(pair(userId, articleId), Updates.setOnInsert("createdAt", new Date()),
					new UpdateOptions().upsert(true));
		}
		catch (MongoException error) {
			if (!DuplicateKeys.isDuplicate(error)) {
				throw error;
			}
		}
	}

	@Override
	public void unfavorite(UUID userId, UUID articleId) {
		favorites.deleteOne(pair(userId, articleId));
	}

	private static Bson pair(UUID userId, UUID articleId) {
		return Filters.and(
				Filters.eq("userId", BsonValues.id(userId)),
				Filters.eq("articleId", BsonValues.id(articleId)));
	}
}
