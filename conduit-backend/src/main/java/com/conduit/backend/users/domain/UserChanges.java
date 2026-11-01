package com.conduit.backend.users.domain;

public record UserChanges(Username username, Email email, String passwordHash, Bio bio, ImageUrl image) {

	public boolean isEmpty() {
		return username == null && email == null && passwordHash == null && bio == null && image == null;
	}
}
