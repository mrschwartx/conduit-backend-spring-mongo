package com.conduit.backend.users.domain;

import com.conduit.backend.shared.domain.Timestamps;
import java.time.Instant;
import java.util.UUID;

public class User {

	private final UUID id;

	private Username username;

	private Email email;

	private String passwordHash;

	private Bio bio;

	private ImageUrl image;

	private final Instant createdAt;

	private Instant updatedAt;

	private User(UUID id, Username username, Email email, String passwordHash, Bio bio, ImageUrl image,
			Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.bio = bio;
		this.image = image;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static User register(UUID id, Username username, Email email, String passwordHash) {
		return register(id, username, email, passwordHash, Timestamps.now());
	}

	public static User register(UUID id, Username username, Email email, String passwordHash, Instant now) {
		return new User(id, username, email, passwordHash, new Bio(null), new ImageUrl(null), now, now);
	}

	public static User restore(UUID id, Username username, Email email, String passwordHash, Bio bio,
			ImageUrl image, Instant createdAt, Instant updatedAt) {
		return new User(id, username, email, passwordHash, bio, image, createdAt, updatedAt);
	}

	public UUID getId() {
		return id;
	}

	public Username getUsername() {
		return username;
	}

	public Email getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Bio getBio() {
		return bio;
	}

	public ImageUrl getImage() {
		return image;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void update(UserChanges changes) {
		update(changes, Timestamps.now());
	}

	public void update(UserChanges changes, Instant now) {
		if (changes.username() != null) {
			this.username = changes.username();
		}
		if (changes.email() != null) {
			this.email = changes.email();
		}
		if (changes.passwordHash() != null) {
			this.passwordHash = changes.passwordHash();
		}
		if (changes.bio() != null) {
			this.bio = changes.bio();
		}
		if (changes.image() != null) {
			this.image = changes.image();
		}
		this.updatedAt = now;
	}
}
