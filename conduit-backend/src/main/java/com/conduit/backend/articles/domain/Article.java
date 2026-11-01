package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.Timestamps;
import java.time.Instant;
import java.util.UUID;

public class Article {

	private final UUID id;

	private final UUID authorId;

	private Slug slug;

	private Title title;

	private Description description;

	private Body body;

	private Tags tags;

	private final Instant createdAt;

	private Instant updatedAt;

	private Article(UUID id, UUID authorId, Slug slug, Title title, Description description, Body body, Tags tags,
			Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.authorId = authorId;
		this.slug = slug;
		this.title = title;
		this.description = description;
		this.body = body;
		this.tags = tags;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Article create(UUID id, UUID authorId, Slug slug, Title title, Description description,
			Body body, Tags tags) {
		return create(id, authorId, slug, title, description, body, tags, Timestamps.now());
	}

	public static Article create(UUID id, UUID authorId, Slug slug, Title title, Description description,
			Body body, Tags tags, Instant now) {
		return new Article(id, authorId, slug, title, description, body, tags, now, now);
	}

	public static Article restore(UUID id, UUID authorId, Slug slug, Title title, Description description,
			Body body, Tags tags, Instant createdAt, Instant updatedAt) {
		return new Article(id, authorId, slug, title, description, body, tags, createdAt, updatedAt);
	}

	public UUID getId() {
		return id;
	}

	public UUID getAuthorId() {
		return authorId;
	}

	public Slug getSlug() {
		return slug;
	}

	public Title getTitle() {
		return title;
	}

	public Description getDescription() {
		return description;
	}

	public Body getBody() {
		return body;
	}

	public Tags getTags() {
		return tags;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public boolean isWrittenBy(UUID userId) {
		return authorId.equals(userId);
	}

	public void update(ArticleChanges changes, Slug newSlug) {
		update(changes, newSlug, Timestamps.now());
	}

	public void update(ArticleChanges changes, Slug newSlug, Instant now) {
		if (changes.title() != null) {
			this.title = changes.title();
		}
		if (newSlug != null) {
			this.slug = newSlug;
		}
		if (changes.description() != null) {
			this.description = changes.description();
		}
		if (changes.body() != null) {
			this.body = changes.body();
		}
		if (changes.tags() != null) {
			this.tags = changes.tags();
		}
		this.updatedAt = now;
	}
}
