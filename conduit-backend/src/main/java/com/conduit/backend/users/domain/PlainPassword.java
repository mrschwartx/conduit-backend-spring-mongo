package com.conduit.backend.users.domain;

import com.conduit.backend.shared.domain.ValidationException;
import java.nio.charset.StandardCharsets;

public record PlainPassword(String value) {

	private static final String FIELD = "password";

	private static final int MIN_LENGTH = 8;

	private static final int MAX_LENGTH = 72;

	private static final int MAX_BYTES = 72;

	public static PlainPassword create(Object raw) {
		if (!(raw instanceof String text) || text.isEmpty()) {
			throw ValidationException.blank(FIELD);
		}
		int length = text.codePointCount(0, text.length());
		if (length < MIN_LENGTH) {
			throw ValidationException.tooShort(FIELD, MIN_LENGTH);
		}
		if (length > MAX_LENGTH || text.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
			throw ValidationException.tooLong(FIELD, MAX_LENGTH);
		}
		return new PlainPassword(text);
	}

	@Override
	public String toString() {
		return "PlainPassword[value=***]";
	}
}
