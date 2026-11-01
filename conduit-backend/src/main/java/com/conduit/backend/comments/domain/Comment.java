package com.conduit.backend.comments.domain;

import com.conduit.backend.shared.domain.Timestamps;
import java.time.Instant;
import java.util.UUID;

public class Comment {

	private final Long id;

	private final UUID articleId;

	private final UUID authorId;

	private final CommentBody body;

	private final Instant createdAt;

	private final Instant updatedAt;

	private Comment(Long id, UUID articleId, UUID authorId, CommentBody body, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.articleId = articleId;
		this.authorId = authorId;
		this.body = body;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Comment create(UUID articleId, UUID authorId, CommentBody body) {
		return create(articleId, authorId, body, Timestamps.now());
	}

	public static Comment create(UUID articleId, UUID authorId, CommentBody body, Instant now) {
		return new Comment(null, articleId, authorId, body, now, now);
	}

	public static Comment restore(Long id, UUID articleId, UUID authorId, CommentBody body, Instant createdAt,
			Instant updatedAt) {
		return new Comment(id, articleId, authorId, body, createdAt, updatedAt);
	}

	public Long getId() {
		return id;
	}

	public UUID getArticleId() {
		return articleId;
	}

	public UUID getAuthorId() {
		return authorId;
	}

	public CommentBody getBody() {
		return body;
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

	public boolean belongsTo(UUID articleId) {
		return this.articleId.equals(articleId);
	}
}
