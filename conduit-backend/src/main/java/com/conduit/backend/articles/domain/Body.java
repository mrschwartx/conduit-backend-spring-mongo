package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.TextRules;

public record Body(String value) {

	private static final String FIELD = "body";

	private static final int MAX_LENGTH = 20_000;

	public static Body create(Object raw) {
		return new Body(TextRules.parse(raw, FIELD, 1, MAX_LENGTH));
	}

	public static Body restore(String value) {
		return new Body(value);
	}
}
