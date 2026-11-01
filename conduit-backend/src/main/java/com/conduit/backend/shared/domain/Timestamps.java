package com.conduit.backend.shared.domain;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class Timestamps {

	private static final DateTimeFormatter ISO_UTC_MILLIS = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
			.withZone(ZoneOffset.UTC);

	private Timestamps() {
	}

	public static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

	public static String format(Instant instant) {
		return ISO_UTC_MILLIS.format(instant);
	}
}
