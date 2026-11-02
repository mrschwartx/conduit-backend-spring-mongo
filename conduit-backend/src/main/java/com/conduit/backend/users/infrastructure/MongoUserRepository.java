package com.conduit.backend.users.infrastructure;

import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.DuplicateKeys;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
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
public class MongoUserRepository implements UserRepository {

	private final MongoCollection<Document> users;

	public MongoUserRepository(MongoDatabase database) {
		this.users = database.getCollection(MongoCollections.USERS);
	}

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	public void add(User user) {
		try {
			users.insertOne(UserDocuments.from(user));
		}
		catch (MongoException error) {
			throw translate(error);
		}
	}

	@Override
	public boolean update(User user) {
		try {
			return users.updateOne(Filters.eq("_id", BsonValues.id(user.getId())), Updates.combine(
					Updates.set("username", user.getUsername().value()),
					Updates.set("usernameLower", BsonValues.lower(user.getUsername().value())),
					Updates.set("email", user.getEmail().value()),
					Updates.set("emailLower", BsonValues.lower(user.getEmail().value())),
					Updates.set("passwordHash", user.getPasswordHash()),
					Updates.set("bio", user.getBio().value()),
					Updates.set("image", user.getImage().value()),
					Updates.set("updatedAt", BsonValues.date(user.getUpdatedAt())))).getMatchedCount() == 1;
		}
		catch (MongoException error) {
			throw translate(error);
		}
	}

	@Override
	public Optional<User> findById(UUID id) {
		return find(Filters.eq("_id", BsonValues.id(id)));
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return find(Filters.eq("emailLower", BsonValues.lower(email)));
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return find(Filters.eq("usernameLower", BsonValues.lower(username)));
	}

	@Override
	public boolean existsByEmail(String email, UUID excludeId) {
		return exists("emailLower", BsonValues.lower(email), excludeId);
	}

	@Override
	public boolean existsByUsername(String username, UUID excludeId) {
		return exists("usernameLower", BsonValues.lower(username), excludeId);
	}

	private Optional<User> find(Bson filter) {
		return Optional.ofNullable(users.find(filter).first()).map(UserDocuments::toDomain);
	}

	private boolean exists(String field, String value, UUID excludeId) {
		Bson filter = excludeId == null
				? Filters.eq(field, value)
				: Filters.and(Filters.eq(field, value), Filters.ne("_id", BsonValues.id(excludeId)));
		return users.countDocuments(filter) > 0;
	}

	private static RuntimeException translate(MongoException error) {
		if (DuplicateKeys.violates(error, "users_email_unique")) {
			return new AlreadyExistsException("email");
		}
		if (DuplicateKeys.violates(error, "users_username_unique")) {
			return new AlreadyExistsException("username");
		}
		if (DuplicateKeys.isDuplicate(error)) {
			return new AlreadyExistsException("username", "email");
		}
		return error;
	}
}
