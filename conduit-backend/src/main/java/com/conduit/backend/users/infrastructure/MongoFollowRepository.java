package com.conduit.backend.users.infrastructure;

import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.DuplicateKeys;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.conduit.backend.users.domain.FollowRepository;
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
public class MongoFollowRepository implements FollowRepository {

	private final MongoCollection<Document> follows;

	public MongoFollowRepository(MongoDatabase database) {
		this.follows = database.getCollection(MongoCollections.FOLLOWS);
	}

	@Override
	public void follow(UUID followerId, UUID followeeId) {
		try {
			follows.updateOne(pair(followerId, followeeId), Updates.setOnInsert("createdAt", new Date()),
					new UpdateOptions().upsert(true));
		}
		catch (MongoException error) {
			if (!DuplicateKeys.isDuplicate(error)) {
				throw error;
			}
		}
	}

	@Override
	public void unfollow(UUID followerId, UUID followeeId) {
		follows.deleteOne(pair(followerId, followeeId));
	}

	@Override
	public boolean isFollowing(UUID followerId, UUID followeeId) {
		return follows.countDocuments(pair(followerId, followeeId)) > 0;
	}

	private static Bson pair(UUID followerId, UUID followeeId) {
		return Filters.and(
				Filters.eq("followerId", BsonValues.id(followerId)),
				Filters.eq("followeeId", BsonValues.id(followeeId)));
	}
}
