package com.conduit.backend.users.domain;

import com.conduit.backend.shared.domain.TextRules;
import com.conduit.backend.shared.domain.ValidationException;

public record Bio(String value) {

	private static final String FIELD = "bio";

	private static final int MAX_LENGTH = 300;

	public static Bio create(Object raw) {
		if (raw == null) {
			return new Bio(null);
		}
		if (!(raw instanceof String text)) {
			throw ValidationException.invalid(FIELD);
		}
		String value = TextRules.trim(text);
		if (value.isEmpty()) {
			return new Bio(null);
		}
		if (value.codePointCount(0, value.length()) > MAX_LENGTH) {
			throw ValidationException.tooLong(FIELD, MAX_LENGTH);
		}
		return new Bio(value);
	}

	public static Bio restore(String value) {
		return new Bio(value);
	}
}
