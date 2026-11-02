package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentRepository;
import com.conduit.backend.shared.infrastructure.persistence.MongoCollections;
import com.conduit.backend.shared.infrastructure.persistence.Sequences;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.Optional;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class MongoCommentRepository implements CommentRepository {

	private static final String SEQUENCE = "comments";

	private final MongoCollection<Document> comments;

	private final Sequences sequences;

	public MongoCommentRepository(MongoDatabase database, Sequences sequences) {
		this.comments = database.getCollection(MongoCollections.COMMENTS);
		this.sequences = sequences;
	}

	@Override
	public Comment add(Comment comment) {
		long id = sequences.next(SEQUENCE);
		comments.insertOne(CommentDocuments.from(id, comment));
		return Comment.restore(id, comment.getArticleId(), comment.getAuthorId(), comment.getBody(),
				comment.getCreatedAt(), comment.getUpdatedAt());
	}

	@Override
	public Optional<Comment> findById(long id) {
		return Optional.ofNullable(comments.find(Filters.eq("_id", id)).first()).map(CommentDocuments::toDomain);
	}

	@Override
	public boolean deleteById(long id) {
		return comments.deleteOne(Filters.eq("_id", id)).getDeletedCount() == 1;
	}
}
