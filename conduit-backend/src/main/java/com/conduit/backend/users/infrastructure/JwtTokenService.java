package com.conduit.backend.users.infrastructure;

import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.users.domain.ExpiredTokenException;
import com.conduit.backend.users.domain.InvalidTokenException;
import com.conduit.backend.users.domain.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtTokenService implements TokenService {

	private static final String ID_CLAIM = "id";

	private static final String ALGORITHM = "HS256";

	private final SecretKey key;

	private final Duration ttl;

	private final Clock clock;

	public JwtTokenService(String secret, Duration ttl) {
		this(secret, ttl, Clock.systemUTC());
	}

	JwtTokenService(String secret, Duration ttl, Clock clock) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.ttl = ttl;
		this.clock = clock;
	}

	@Override
	public String issue(UUID userId) {
		Instant now = clock.instant();
		return Jwts.builder()
				.claim(ID_CLAIM, userId.toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(ttl)))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}

	@Override
	public UUID verify(String token) {
		try {
			Jws<Claims> jws = Jwts.parser()
					.verifyWith(key)
					.clock(() -> Date.from(clock.instant()))
					.build()
					.parseSignedClaims(token);
			if (!ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
				throw new InvalidTokenException("Unsupported token algorithm");
			}
			Claims claims = jws.getPayload();
			if (claims.getExpiration() == null) {
				throw new InvalidTokenException("Token has no expiration");
			}
			Object id = claims.get(ID_CLAIM);
			if (!EntityIds.isValid(id)) {
				throw new InvalidTokenException("Invalid token payload");
			}
			return UUID.fromString(((String) id).toLowerCase(Locale.ROOT));
		}
		catch (ExpiredJwtException error) {
			throw new ExpiredTokenException("Token expired", error);
		}
		catch (JwtException | IllegalArgumentException error) {
			throw new InvalidTokenException("Invalid token", error);
		}
	}
}