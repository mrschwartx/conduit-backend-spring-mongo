package com.conduit.backend.users.domain;

public class ExpiredTokenException extends InvalidTokenException {

	public ExpiredTokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
