package com.conduit.backend.users.domain;

import com.conduit.backend.shared.domain.TextRules;
import com.conduit.backend.shared.domain.ValidationException;
import java.util.regex.Pattern;

public record Username(String value) {

	private static final String FIELD = "username";

	private static final int MIN_LENGTH = 3;

	private static final int MAX_LENGTH = 30;

	private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z0-9_-]+$");

	public static Username create(Object raw) {
		String value = TextRules.parse(raw, FIELD, MIN_LENGTH, MAX_LENGTH);
		if (!ALLOWED.matcher(value).matches()) {
			throw ValidationException.invalid(FIELD);
		}
		return new Username(value);
	}

	public static Username restore(String value) {
		return new Username(value);
	}
}
