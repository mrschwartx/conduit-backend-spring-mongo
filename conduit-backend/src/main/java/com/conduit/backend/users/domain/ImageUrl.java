package com.conduit.backend.users.domain;

import com.conduit.backend.shared.domain.TextRules;
import com.conduit.backend.shared.domain.ValidationException;
import java.net.URI;
import java.util.Locale;

public record ImageUrl(String value) {

	private static final String FIELD = "image";

	private static final int MAX_LENGTH = 2048;

	public static ImageUrl create(Object raw) {
		if (raw == null) {
			return new ImageUrl(null);
		}
		if (!(raw instanceof String text)) {
			throw ValidationException.invalid(FIELD);
		}
		String value = TextRules.trim(text);
		if (value.isEmpty()) {
			return new ImageUrl(null);
		}
		if (value.length() > MAX_LENGTH) {
			throw ValidationException.tooLong(FIELD, MAX_LENGTH);
		}
		if (!isWebAddress(value)) {
			throw ValidationException.invalid(FIELD);
		}
		return new ImageUrl(value);
	}

	public static ImageUrl restore(String value) {
		return new ImageUrl(value);
	}

	private static boolean isWebAddress(String value) {
		try {
			URI uri = URI.create(value);
			String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
			return scheme.equals("https") && uri.getHost() != null;
		}
		catch (IllegalArgumentException error) {
			return false;
		}
	}
}
