package com.conduit.backend.comments.application;

import com.conduit.backend.comments.domain.ArticleLookup;
import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentRepository;
import com.conduit.backend.shared.domain.ForbiddenException;
import com.conduit.backend.shared.domain.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeleteComment {

	private final ArticleLookup articleLookup;

	private final CommentRepository commentRepository;

	public DeleteComment(ArticleLookup articleLookup, CommentRepository commentRepository) {
		this.articleLookup = articleLookup;
		this.commentRepository = commentRepository;
	}

	public void execute(UUID requesterId, String slug, String commentId) {
		UUID articleId = articleLookup.findIdBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
		long id = parseId(commentId);
		Comment comment = commentRepository.findById(id).orElseThrow(() -> new NotFoundException("comment"));
		if (!comment.belongsTo(articleId)) {
			throw new NotFoundException("comment");
		}
		if (!comment.isWrittenBy(requesterId)) {
			throw new ForbiddenException("comment");
		}
		if (!commentRepository.deleteById(id)) {
			throw new NotFoundException("comment");
		}
	}

	private static long parseId(String raw) {
		try {
			return Long.parseLong(raw);
		}
		catch (NumberFormatException error) {
			throw new NotFoundException("comment");
		}
	}
}
