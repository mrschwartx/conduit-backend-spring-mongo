package com.conduit.backend.shared.domain;

import java.util.UUID;
import java.util.regex.Pattern;

public final class EntityIds {

	private static final Pattern ENTITY_ID_PATTERN = Pattern
			.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

	private EntityIds() {
	}

	public static UUID generate() {
		return UUID.randomUUID();
	}

	public static boolean isValid(Object value) {
		return value instanceof String text && ENTITY_ID_PATTERN.matcher(text).matches();
	}
}
