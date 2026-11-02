package com.conduit.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.conduit.backend.shared.infrastructure.persistence.DuplicateKeys;
import com.mongodb.MongoException;
import org.junit.jupiter.api.Test;

class DuplicateKeysTest {

	private static final int DUPLICATE_KEY = 11000;

	@Test
	void recognisesDuplicateKeyErrors() {
		MongoException error = new MongoException(DUPLICATE_KEY, "E11000 duplicate key index: users_email_unique");

		assertThat(DuplicateKeys.isDuplicate(error)).isTrue();
	}

	@Test
	void findsDuplicateKeyErrorsInTheCauseChain() {
		MongoException cause = new MongoException(DUPLICATE_KEY, "E11000 duplicate key index: users_username_unique");

		assertThat(DuplicateKeys.isDuplicate(new IllegalStateException("wrapped", cause))).isTrue();
	}

	@Test
	void matchesTheViolatedIndexByName() {
		MongoException error = new MongoException(DUPLICATE_KEY, "E11000 duplicate key index: users_email_unique");

		assertThat(DuplicateKeys.violates(error, "users_email_unique")).isTrue();
		assertThat(DuplicateKeys.violates(error, "users_username_unique")).isFalse();
	}

	@Test
	void ignoresOtherErrors() {
		assertThat(DuplicateKeys.isDuplicate(new MongoException(13, "unauthorized"))).isFalse();
		assertThat(DuplicateKeys.isDuplicate(new IllegalStateException("boom"))).isFalse();
	}
}
