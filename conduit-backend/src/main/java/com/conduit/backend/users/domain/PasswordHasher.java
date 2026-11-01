package com.conduit.backend.users.domain;

public interface PasswordHasher {

	String hash(String plain);

	boolean matches(String plain, String hash);
}
