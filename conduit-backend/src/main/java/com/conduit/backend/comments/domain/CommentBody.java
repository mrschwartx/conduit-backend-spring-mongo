package com.conduit.backend.comments.domain;

import com.conduit.backend.shared.domain.TextRules;

public record CommentBody(String value) {

	private static final String FIELD = "body";

	private static final int MAX_LENGTH = 1000;

	public static CommentBody create(Object raw) {
		return new CommentBody(TextRules.parse(raw, FIELD, 1, MAX_LENGTH));
	}

	public static CommentBody restore(String value) {
		return new CommentBody(value);
	}
}
