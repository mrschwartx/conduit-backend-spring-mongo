package com.conduit.backend.users.application;

import com.conduit.backend.shared.application.AuthenticatedUser;
import com.conduit.backend.shared.domain.AuthenticationFailedException;
import com.conduit.backend.users.domain.ExpiredTokenException;
import com.conduit.backend.users.domain.InvalidTokenException;
import com.conduit.backend.users.domain.TokenService;
import org.springframework.stereotype.Service;

@Service
public class VerifyAccessToken {

	private final TokenService tokenService;

	public VerifyAccessToken(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	public AuthenticatedUser execute(String token) {
		if (token == null || token.isEmpty()) {
			throw new AuthenticationFailedException("token", "is missing");
		}
		try {
			return new AuthenticatedUser(tokenService.verify(token));
		}
		catch (ExpiredTokenException error) {
			throw new AuthenticationFailedException("token", "has expired");
		}
		catch (InvalidTokenException error) {
			throw new AuthenticationFailedException("token", "is invalid");
		}
	}
}
