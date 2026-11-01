package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.TextRules;
import com.conduit.backend.shared.domain.ValidationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

public record Tags(List<String> values) {

	private static final String FIELD = "tagList";

	private static final int MAX_TAGS = 10;

	private static final int MAX_TAG_LENGTH = 30;

	private static final Pattern FORBIDDEN = Pattern.compile("[\\s,]");

	public Tags {
		values = List.copyOf(values);
	}

	public static Tags empty() {
		return new Tags(List.of());
	}

	public static Tags create(Object raw) {
		if (raw == null) {
			return empty();
		}
		if (!(raw instanceof List<?> items)) {
			throw ValidationException.invalid(FIELD);
		}
		LinkedHashSet<String> unique = new LinkedHashSet<>();
		for (Object item : items) {
			if (!(item instanceof String text)) {
				throw ValidationException.invalid(FIELD);
			}
			String tag = TextRules.trim(text);
			if (tag.isEmpty() || FORBIDDEN.matcher(tag).find()) {
				throw ValidationException.invalid(FIELD);
			}
			if (tag.codePointCount(0, tag.length()) > MAX_TAG_LENGTH) {
				throw ValidationException.tooLong(FIELD, MAX_TAG_LENGTH);
			}
			unique.add(tag);
		}
		if (unique.size() > MAX_TAGS) {
			throw new ValidationException(FIELD, "is too long (maximum is " + MAX_TAGS + " tags)");
		}
		return new Tags(List.copyOf(unique));
	}

	public static Tags restore(List<String> values) {
		return new Tags(values);
	}
}
