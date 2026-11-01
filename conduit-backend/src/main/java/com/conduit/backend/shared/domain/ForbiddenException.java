package com.conduit.backend.shared.domain;

public class ForbiddenException extends DomainException {

	public ForbiddenException(String resource) {
		super("FORBIDDEN", resource, "forbidden");
	}
}
