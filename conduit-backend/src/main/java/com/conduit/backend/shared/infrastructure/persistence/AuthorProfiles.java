package com.conduit.backend.shared.infrastructure.persistence;

import com.conduit.backend.shared.domain.Profile;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class AuthorProfiles {

	private final MongoCollection<Document> users;

	private final MongoCollection<Document> follows;

	public AuthorProfiles(MongoDatabase database) {
		this.users = database.getCollection(MongoCollections.USERS);
		this.follows = database.getCollection(MongoCollections.FOLLOWS);
	}

	public Map<UUID, Profile> load(Collection<UUID> authorIds, UUID viewerId) {
		if (authorIds.isEmpty()) {
			return Map.of();
		}
		List<String> ids = BsonValues.ids(authorIds);
		Set<UUID> followed = viewerId == null ? Set.of() : followed(ids, viewerId);
		Map<UUID, Profile> profiles = new HashMap<>();
		List<Document> rows = users.find(Filters.in("_id", ids))
				.projection(Projections.include("username", "bio", "image"))
				.into(new ArrayList<>());
		for (Document row : rows) {
			UUID id = BsonValues.uuid(row, "_id");
			profiles.put(id, new Profile(row.getString("username"), row.getString("bio"),
					row.getString("image"), followed.contains(id)));
		}
		return profiles;
	}

	private Set<UUID> followed(List<String> authorIds, UUID viewerId) {
		Set<UUID> followed = new HashSet<>();
		List<Document> rows = follows.find(Filters.and(
				Filters.eq("followerId", BsonValues.id(viewerId)),
				Filters.in("followeeId", authorIds)))
				.projection(Projections.include("followeeId"))
				.into(new ArrayList<>());
		for (Document row : rows) {
			followed.add(BsonValues.uuid(row, "followeeId"));
		}
		return followed;
	}
}
