package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.TextRules;

public record Description(String value) {

	private static final String FIELD = "description";

	private static final int MAX_LENGTH = 200;

	public static Description create(Object raw) {
		return new Description(TextRules.parse(raw, FIELD, 1, MAX_LENGTH));
	}

	public static Description restore(String value) {
		return new Description(value);
	}
}
