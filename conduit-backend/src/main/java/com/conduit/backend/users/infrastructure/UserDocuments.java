package com.conduit.backend.users.infrastructure;

import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.users.domain.Bio;
import com.conduit.backend.users.domain.Email;
import com.conduit.backend.users.domain.ImageUrl;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.Username;
import org.bson.Document;

final class UserDocuments {

	private UserDocuments() {
	}

	static Document from(User user) {
		return new Document("_id", BsonValues.id(user.getId()))
				.append("username", user.getUsername().value())
				.append("usernameLower", BsonValues.lower(user.getUsername().value()))
				.append("email", user.getEmail().value())
				.append("emailLower", BsonValues.lower(user.getEmail().value()))
				.append("passwordHash", user.getPasswordHash())
				.append("bio", user.getBio().value())
				.append("image", user.getImage().value())
				.append("createdAt", BsonValues.date(user.getCreatedAt()))
				.append("updatedAt", BsonValues.date(user.getUpdatedAt()));
	}

	static User toDomain(Document document) {
		return User.restore(
				BsonValues.uuid(document, "_id"),
				Username.restore(document.getString("username")),
				Email.restore(document.getString("email")),
				document.getString("passwordHash"),
				Bio.restore(document.getString("bio")),
				ImageUrl.restore(document.getString("image")),
				BsonValues.instant(document, "createdAt"),
				BsonValues.instant(document, "updatedAt"));
	}
}
