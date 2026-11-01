package com.conduit.backend.articles.domain;

import java.text.Normalizer;
import java.util.Locale;

public record Slug(String value) {

	public static final String RESERVED = "feed";

	private static final int MAX_LENGTH = 80;

	private static final String FALLBACK = "article";

	public static Slug base(Title title) {
		String plain = Normalizer.normalize(title.value(), Normalizer.Form.NFKD).replaceAll("\\p{M}+", "");
		String base = plain.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
		if (base.length() > MAX_LENGTH) {
			base = base.substring(0, MAX_LENGTH).replaceAll("-+$", "");
		}
		return new Slug(base.isEmpty() ? FALLBACK : base);
	}

	public static Slug restore(String value) {
		return new Slug(value);
	}

	public Slug withSuffix(int number) {
		return new Slug(value + "-" + number);
	}

	public boolean isReserved() {
		return RESERVED.equals(value);
	}
}
