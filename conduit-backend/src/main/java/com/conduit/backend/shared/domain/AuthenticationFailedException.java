package com.conduit.backend.shared.domain;

public class AuthenticationFailedException extends DomainException {

	public AuthenticationFailedException(String field, String message) {
		super("AUTHENTICATION_FAILED", field, message);
	}
}
