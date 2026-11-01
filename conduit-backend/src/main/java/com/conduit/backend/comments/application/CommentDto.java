package com.conduit.backend.comments.application;

import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentView;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.domain.Timestamps;

public record CommentDto(long id, String createdAt, String updatedAt, String body, Profile author) {

	public static CommentDto from(CommentView view) {
		Comment comment = view.comment();
		return new CommentDto(
				comment.getId(),
				Timestamps.format(comment.getCreatedAt()),
				Timestamps.format(comment.getUpdatedAt()),
				comment.getBody().value(),
				view.author());
	}
}
