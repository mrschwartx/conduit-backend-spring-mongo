package com.conduit.backend.shared.domain;

public record PageWindow(int offset, int limit) {

	public static final int DEFAULT_LIMIT = 20;

	public static final int MAX_LIMIT = 50;

	public static PageWindow parse(String offset, String limit) {
		int parsedOffset = offset == null || offset.isBlank() ? 0 : number(offset, "offset");
		if (parsedOffset < 0) {
			throw ValidationException.invalid("offset");
		}
		int parsedLimit = limit == null || limit.isBlank() ? DEFAULT_LIMIT : number(limit, "limit");
		if (parsedLimit < 1) {
			throw ValidationException.invalid("limit");
		}
		return new PageWindow(parsedOffset, Math.min(parsedLimit, MAX_LIMIT));
	}

	private static int number(String value, String field) {
		try {
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException error) {
			throw ValidationException.invalid(field);
		}
	}
}
