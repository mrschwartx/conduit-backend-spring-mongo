package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.TextRules;

public record Title(String value) {

	private static final String FIELD = "title";

	private static final int MAX_LENGTH = 100;

	public static Title create(Object raw) {
		return new Title(TextRules.parse(raw, FIELD, 1, MAX_LENGTH));
	}

	public static Title restore(String value) {
		return new Title(value);
	}
}
