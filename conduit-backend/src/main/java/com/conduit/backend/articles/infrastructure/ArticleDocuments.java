package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.Body;
import com.conduit.backend.articles.domain.Description;
import com.conduit.backend.articles.domain.Slug;
import com.conduit.backend.articles.domain.Tags;
import com.conduit.backend.articles.domain.Title;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import java.util.List;
import org.bson.Document;

final class ArticleDocuments {

	private ArticleDocuments() {
	}

	static Document from(Article article) {
		return new Document("_id", BsonValues.id(article.getId()))
				.append("authorId", BsonValues.id(article.getAuthorId()))
				.append("slug", article.getSlug().value())
				.append("title", article.getTitle().value())
				.append("description", article.getDescription().value())
				.append("body", article.getBody().value())
				.append("tags", article.getTags().values())
				.append("createdAt", BsonValues.date(article.getCreatedAt()))
				.append("updatedAt", BsonValues.date(article.getUpdatedAt()));
	}

	static Article toDomain(Document document) {
		return Article.restore(
				BsonValues.uuid(document, "_id"),
				BsonValues.uuid(document, "authorId"),
				Slug.restore(document.getString("slug")),
				Title.restore(document.getString("title")),
				Description.restore(document.getString("description")),
				Body.restore(document.getString("body")),
				Tags.restore(document.getList("tags", String.class, List.of())),
				BsonValues.instant(document, "createdAt"),
				BsonValues.instant(document, "updatedAt"));
	}
}
