package com.conduit.backend.users.domain;

import com.conduit.backend.shared.domain.TextRules;
import com.conduit.backend.shared.domain.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

	private static final String FIELD = "email";

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
			Pattern.UNICODE_CHARACTER_CLASS);

	private static final int MAX_LENGTH = 254;

	public static Email create(Object raw) {
		if (!(raw instanceof String text)) {
			throw ValidationException.blank(FIELD);
		}
		String value = TextRules.trim(text);
		if (value.isEmpty()) {
			throw ValidationException.blank(FIELD);
		}
		if (value.codePointCount(0, value.length()) > MAX_LENGTH || !EMAIL_PATTERN.matcher(value).matches()) {
			throw ValidationException.invalid(FIELD);
		}
		return new Email(value.toLowerCase(Locale.ROOT));
	}

	public static Email restore(String value) {
		return new Email(value);
	}
}
