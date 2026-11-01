package com.conduit.backend.shared.domain;

import java.util.List;
import java.util.Map;

public class ValidationException extends DomainException {

	public ValidationException(String field, String message) {
		super("VALIDATION_ERROR", field, message);
	}

	public ValidationException(Map<String, List<String>> errors) {
		super("VALIDATION_ERROR", errors);
	}

	public static ValidationException blank(String field) {
		return new ValidationException(field, "can't be blank");
	}

	public static ValidationException invalid(String field) {
		return new ValidationException(field, "is invalid");
	}

	public static ValidationException tooShort(String field, int minimum) {
		return new ValidationException(field, "is too short (minimum is " + minimum + " characters)");
	}

	public static ValidationException tooLong(String field, int maximum) {
		return new ValidationException(field, "is too long (maximum is " + maximum + " characters)");
	}
}
