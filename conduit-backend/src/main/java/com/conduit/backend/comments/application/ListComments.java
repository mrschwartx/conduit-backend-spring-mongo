package com.conduit.backend.comments.application;

import com.conduit.backend.comments.domain.ArticleLookup;
import com.conduit.backend.comments.domain.CommentQueries;
import com.conduit.backend.shared.domain.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListComments {

	private final ArticleLookup articleLookup;

	private final CommentQueries commentQueries;

	public ListComments(ArticleLookup articleLookup, CommentQueries commentQueries) {
		this.articleLookup = articleLookup;
		this.commentQueries = commentQueries;
	}

	public List<CommentDto> execute(UUID viewerId, String slug) {
		UUID articleId = articleLookup.findIdBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
		return commentQueries.findByArticle(articleId, viewerId).stream().map(CommentDto::from).toList();
	}
}
