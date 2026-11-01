package com.conduit.backend.shared.domain;

public final class TextRules {

	private TextRules() {
	}

	public static String trim(String value) {
		int start = 0;
		int end = value.length();
		while (start < end && isTrimmable(value.charAt(start))) {
			start++;
		}
		while (end > start && isTrimmable(value.charAt(end - 1))) {
			end--;
		}
		return value.substring(start, end);
	}

	public static String parse(Object raw, String field, int minLength, int maxLength) {
		if (!(raw instanceof String text)) {
			throw ValidationException.blank(field);
		}
		String value = trim(text);
		if (value.isEmpty() && minLength > 0) {
			throw ValidationException.blank(field);
		}
		int length = value.codePointCount(0, value.length());
		if (length < minLength) {
			throw ValidationException.tooShort(field, minLength);
		}
		if (length > maxLength) {
			throw ValidationException.tooLong(field, maxLength);
		}
		return value;
	}

	private static boolean isTrimmable(char character) {
		return Character.isWhitespace(character) || Character.isSpaceChar(character) || character == '\uFEFF';
	}
}
