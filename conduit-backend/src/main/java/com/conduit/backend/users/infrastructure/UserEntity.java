package com.conduit.backend.users.infrastructure;

import com.conduit.backend.users.domain.Bio;
import com.conduit.backend.users.domain.Email;
import com.conduit.backend.users.domain.ImageUrl;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "username", nullable = false, length = 50)
	private String username;

	@Column(name = "email", nullable = false, length = 254)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "bio")
	private String bio;

	@Column(name = "image", length = 2048)
	private String image;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected UserEntity() {
	}

	static UserEntity from(User user) {
		UserEntity entity = new UserEntity();
		entity.id = user.getId();
		entity.createdAt = user.getCreatedAt();
		entity.apply(user);
		return entity;
	}

	void apply(User user) {
		this.username = user.getUsername().value();
		this.email = user.getEmail().value();
		this.passwordHash = user.getPasswordHash();
		this.bio = user.getBio().value();
		this.image = user.getImage().value();
		this.updatedAt = user.getUpdatedAt();
	}

	User toDomain() {
		return User.restore(id, Username.restore(username), Email.restore(email), passwordHash, Bio.restore(bio),
				ImageUrl.restore(image), createdAt, updatedAt);
	}
}
