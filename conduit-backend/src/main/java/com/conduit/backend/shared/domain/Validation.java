package com.conduit.backend.shared.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Validation {

	private final Map<String, List<String>> errors = new LinkedHashMap<>();

	public <T> T check(Supplier<T> parser) {
		try {
			return parser.get();
		}
		catch (ValidationException error) {
			error.getErrors().forEach((field, messages) -> errors
					.computeIfAbsent(field, key -> new ArrayList<>())
					.addAll(messages));
			return null;
		}
	}

	public void failIfAny() {
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
}
