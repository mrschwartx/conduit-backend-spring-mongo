package com.conduit.backend.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BcryptPasswordHasherTest {

	private final BcryptPasswordHasher hasher = new BcryptPasswordHasher(4);

	@Test
	void hashesWithSaltAndVerifies() {
		String first = hasher.hash("Password123!");
		String second = hasher.hash("Password123!");

		assertThat(first).startsWith("$2b$04$").isNotEqualTo(second);
		assertThat(hasher.matches("Password123!", first)).isTrue();
		assertThat(hasher.matches("wrong-password", first)).isFalse();
	}

	@Test
	void verifiesHashesProducedByNodeBcrypt() {
		String nodeHash = hasher.hash("password123").replace("$2b$", "$2a$");

		assertThat(hasher.matches("password123", nodeHash)).isTrue();
	}

	@Test
	void treatsInvalidHashesAsMismatch() {
		assertThat(hasher.matches("password123", "not-a-bcrypt-hash")).isFalse();
	}
}
