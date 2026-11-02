package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentQueries;
import com.conduit.backend.comments.domain.CommentView;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.infrastructure.persistence.AuthorProfiles;
import com.conduit.backend.shared.infrastructure.persistence.BsonValues;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class MongoCommentQueries implements CommentQueries {

	private final MongoCollection<Document> comments;

	private final AuthorProfiles authorProfiles;

	public MongoCommentQueries(MongoDatabase database, AuthorProfiles authorProfiles) {
		this.comments = database.getCollection(MongoCollections.COMMENTS);
		this.authorProfiles = authorProfiles;
	}

	@Override
	public List<CommentView> findByArticle(UUID articleId, UUID viewerId) {
		List<Document> documents = comments.find(Filters.eq("articleId", BsonValues.id(articleId)))
				.sort(Sorts.orderBy(Sorts.descending("createdAt"), Sorts.descending("_id")))
				.into(new ArrayList<>());
		List<Comment> found = documents.stream().map(CommentDocuments::toDomain).toList();
		Set<UUID> authorIds = new HashSet<>();
		found.forEach(comment -> authorIds.add(comment.getAuthorId()));
		Map<UUID, Profile> authors = authorProfiles.load(authorIds, viewerId);
		return found.stream().map(comment -> new CommentView(comment, authors.get(comment.getAuthorId()))).toList();
	}

	@Override
	public CommentView viewOf(Comment comment, UUID viewerId) {
		Map<UUID, Profile> authors = authorProfiles.load(Set.of(comment.getAuthorId()), viewerId);
		return new CommentView(comment, authors.get(comment.getAuthorId()));
	}
}
