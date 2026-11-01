package com.conduit.backend.shared.infrastructure.http;

import com.conduit.backend.shared.domain.ValidationException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RequestBodies {

	private RequestBodies() {
	}

	public static Map<String, Object> section(Map<String, Object> body, String key) {
		if (body == null || !(body.get(key) instanceof Map<?, ?> raw)) {
			throw ValidationException.blank(key);
		}
		Map<String, Object> section = new LinkedHashMap<>();
		raw.forEach((name, value) -> section.put(String.valueOf(name), value));
		return section;
	}
}
