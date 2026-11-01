package com.conduit.backend.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.users.domain.ExpiredTokenException;
import com.conduit.backend.users.domain.InvalidTokenException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

	private static final String SECRET = "0123456789abcdef0123456789abcdef";

	private static final Instant NOW = Instant.parse("2026-10-25T00:00:00Z");

	private static JwtTokenService service(String secret, Instant now) {
		return new JwtTokenService(secret, Duration.ofHours(1), Clock.fixed(now, ZoneOffset.UTC));
	}

	@Test
	void issuesTokensThatVerifyToTheSameUser() {
		UUID userId = UUID.randomUUID();
		JwtTokenService service = service(SECRET, NOW);

		assertThat(service.verify(service.issue(userId))).isEqualTo(userId);
	}

	@Test
	void producesThreePartHs256Tokens() {
		String token = service(SECRET, NOW).issue(UUID.randomUUID());

		assertThat(token.split("\\.")).hasSize(3);
	}

	@Test
	void rejectsExpiredTokens() {
		String token = service(SECRET, NOW).issue(UUID.randomUUID());

		assertThatThrownBy(() -> service(SECRET, NOW.plus(Duration.ofHours(2))).verify(token))
				.isInstanceOf(ExpiredTokenException.class);
	}

	@Test
	void rejectsTokensSignedWithAnotherSecret() {
		String token = service("fedcba9876543210fedcba9876543210", NOW).issue(UUID.randomUUID());

		assertThatThrownBy(() -> service(SECRET, NOW).verify(token)).isInstanceOf(InvalidTokenException.class);
	}

	@Test
	void rejectsTamperedAndMalformedTokens() {
		JwtTokenService service = service(SECRET, NOW);
		String token = service.issue(UUID.randomUUID());
		String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

		assertThatThrownBy(() -> service.verify(tampered)).isInstanceOf(InvalidTokenException.class);
		assertThatThrownBy(() -> service.verify("garbage")).isInstanceOf(InvalidTokenException.class);
		assertThatThrownBy(() -> service.verify("a.b.c")).isInstanceOf(InvalidTokenException.class);
	}
}
