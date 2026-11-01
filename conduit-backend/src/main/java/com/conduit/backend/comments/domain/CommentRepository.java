package com.conduit.backend.comments.domain;

import java.util.Optional;

public interface CommentRepository {

	Comment add(Comment comment);

	Optional<Comment> findById(long id);

	boolean deleteById(long id);
}
