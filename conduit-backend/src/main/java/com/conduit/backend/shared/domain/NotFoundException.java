package com.conduit.backend.shared.domain;

public class NotFoundException extends DomainException {

	public NotFoundException(String resource) {
		super("NOT_FOUND", resource, "not found");
	}
}
