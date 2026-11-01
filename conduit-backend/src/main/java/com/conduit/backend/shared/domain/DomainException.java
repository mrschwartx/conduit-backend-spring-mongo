package com.conduit.backend.shared.domain;

import java.util.List;
import java.util.Map;

public abstract class DomainException extends RuntimeException {

	private final String code;

	private final Map<String, List<String>> errors;

	protected DomainException(String code, String field, String message) {
		this(code, Map.of(field, List.of(message)));
	}

	protected DomainException(String code, Map<String, List<String>> errors) {
		super(errors.toString());
		this.code = code;
		this.errors = Map.copyOf(errors);
	}

	public String getCode() {
		return code;
	}

	public Map<String, List<String>> getErrors() {
		return errors;
	}
}
