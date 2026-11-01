package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.Body;
import com.conduit.backend.articles.domain.Description;
import com.conduit.backend.articles.domain.Slug;
import com.conduit.backend.articles.domain.Tags;
import com.conduit.backend.articles.domain.Title;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "articles")
public class ArticleEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "author_id", nullable = false, updatable = false)
	private UUID authorId;

	@Column(name = "slug", nullable = false, length = 255)
	private String slug;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "description", nullable = false, length = 1000)
	private String description;

	@Column(name = "body", nullable = false)
	private String body;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected ArticleEntity() {
	}

	static ArticleEntity from(Article article) {
		ArticleEntity entity = new ArticleEntity();
		entity.id = article.getId();
		entity.authorId = article.getAuthorId();
		entity.createdAt = article.getCreatedAt();
		entity.apply(article);
		return entity;
	}

	void apply(Article article) {
		this.slug = article.getSlug().value();
		this.title = article.getTitle().value();
		this.description = article.getDescription().value();
		this.body = article.getBody().value();
		this.updatedAt = article.getUpdatedAt();
	}

	UUID getId() {
		return id;
	}

	UUID getAuthorId() {
		return authorId;
	}

	Article toDomain(List<String> tags) {
		return Article.restore(id, authorId, Slug.restore(slug), Title.restore(title),
				Description.restore(description), Body.restore(body), Tags.restore(tags), createdAt, updatedAt);
	}
}
