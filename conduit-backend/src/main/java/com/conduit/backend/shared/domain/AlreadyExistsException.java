package com.conduit.backend.shared.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlreadyExistsException extends DomainException {

	public AlreadyExistsException(String... fields) {
		super("ALREADY_EXISTS", taken(fields));
	}

	private static Map<String, List<String>> taken(String[] fields) {
		Map<String, List<String>> errors = new LinkedHashMap<>();
		for (String field : fields) {
			errors.put(field, List.of("has already been taken"));
		}
		return errors;
	}
}
