package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentBody;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import org.bson.Document;

final class CommentDocuments {

	private CommentDocuments() {
	}

	static Document from(long id, Comment comment) {
		return new Document("_id", id)
				.append("articleId", BsonValues.id(comment.getArticleId()))
				.append("authorId", BsonValues.id(comment.getAuthorId()))
				.append("body", comment.getBody().value())
				.append("createdAt", BsonValues.date(comment.getCreatedAt()))
				.append("updatedAt", BsonValues.date(comment.getUpdatedAt()));
	}

	static Comment toDomain(Document document) {
		return Comment.restore(
				((Number) document.get("_id")).longValue(),
				BsonValues.uuid(document, "articleId"),
				BsonValues.uuid(document, "authorId"),
				CommentBody.restore(document.getString("body")),
				BsonValues.instant(document, "createdAt"),
				BsonValues.instant(document, "updatedAt"));
	}
}
