package com.conduit.backend.comments.application;

import com.conduit.backend.comments.domain.ArticleLookup;
import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentBody;
import com.conduit.backend.comments.domain.CommentQueries;
import com.conduit.backend.comments.domain.CommentRepository;
import com.conduit.backend.shared.domain.NotFoundException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AddComment {

	private final ArticleLookup articleLookup;

	private final CommentRepository commentRepository;

	private final CommentQueries commentQueries;

	public AddComment(ArticleLookup articleLookup, CommentRepository commentRepository,
			CommentQueries commentQueries) {
		this.articleLookup = articleLookup;
		this.commentRepository = commentRepository;
		this.commentQueries = commentQueries;
	}

	public CommentDto execute(UUID authorId, String slug, Map<String, Object> input) {
		UUID articleId = articleLookup.findIdBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
		CommentBody body = CommentBody.create(input.get("body"));
		Comment saved = commentRepository.add(Comment.create(articleId, authorId, body));
		return CommentDto.from(commentQueries.viewOf(saved, authorId));
	}
}
