package com.conduit.backend.comments.domain;

import java.util.List;
import java.util.UUID;

public interface CommentQueries {

	List<CommentView> findByArticle(UUID articleId, UUID viewerId);

	CommentView viewOf(Comment comment, UUID viewerId);
}
