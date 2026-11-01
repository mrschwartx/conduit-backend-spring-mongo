package com.conduit.backend.users.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

	private static final Instant CREATED = Instant.parse("2026-10-25T00:00:00Z");

	private static User user() {
		return User.register(UUID.randomUUID(), new Username("john"), new Email("john@mail.com"), "hash", CREATED);
	}

	@Test
	void registersWithoutBioAndImage() {
		User user = user();

		assertThat(user.getBio().value()).isNull();
		assertThat(user.getImage().value()).isNull();
		assertThat(user.getCreatedAt()).isEqualTo(CREATED);
		assertThat(user.getUpdatedAt()).isEqualTo(CREATED);
	}

	@Test
	void appliesOnlyProvidedChanges() {
		User user = user();
		Instant later = CREATED.plusSeconds(60);

		user.update(new UserChanges(new Username("johnny"), null, null, new Bio("hi"), null), later);

		assertThat(user.getUsername().value()).isEqualTo("johnny");
		assertThat(user.getEmail().value()).isEqualTo("john@mail.com");
		assertThat(user.getPasswordHash()).isEqualTo("hash");
		assertThat(user.getBio().value()).isEqualTo("hi");
		assertThat(user.getUpdatedAt()).isEqualTo(later);
	}

	@Test
	void canClearBioAndImage() {
		User user = user();
		user.update(new UserChanges(null, null, null, new Bio("hi"), new ImageUrl("https://a.com/x.png")), CREATED);

		user.update(new UserChanges(null, null, null, new Bio(null), new ImageUrl(null)), CREATED);

		assertThat(user.getBio().value()).isNull();
		assertThat(user.getImage().value()).isNull();
	}
}
