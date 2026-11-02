package com.conduit.backend.shared.infrastructure.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bson.Document;

public final class BsonValues {

	private BsonValues() {
	}

	public static String id(UUID id) {
		return id.toString();
	}

	public static List<String> ids(Collection<UUID> ids) {
		return ids.stream().map(UUID::toString).toList();
	}

	public static UUID uuid(Document document, String field) {
		return UUID.fromString(document.getString(field));
	}

	public static Date date(Instant instant) {
		return Date.from(instant);
	}

	public static Instant instant(Document document, String field) {
		return document.getDate(field).toInstant();
	}

	public static String lower(String value) {
		return value.toLowerCase(Locale.ROOT);
	}
}
