package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.AuthenticationFailedException;
import com.conduit.backend.shared.domain.TextRules;
import com.conduit.backend.shared.domain.ValidationException;
import com.conduit.backend.users.domain.PasswordHasher;
import com.conduit.backend.users.domain.TokenService;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUser {

	private static final String FIELD = "credentials";

	private static final String MESSAGE = "are invalid";

	private final UserRepository userRepository;

	private final PasswordHasher passwordHasher;

	private final TokenService tokenService;

	public AuthenticateUser(UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
	}

	public UserDto execute(Map<String, Object> input) {
		if (!(input.get("email") instanceof String email) || TextRules.trim(email).isEmpty()) {
			throw ValidationException.blank("email");
		}
		if (!(input.get("password") instanceof String password) || password.isEmpty()) {
			throw ValidationException.blank("password");
		}

		User user = userRepository.findByEmail(TextRules.trim(email))
				.orElseThrow(() -> new AuthenticationFailedException(FIELD, MESSAGE));

		if (!passwordHasher.matches(password, user.getPasswordHash())) {
			throw new AuthenticationFailedException(FIELD, MESSAGE);
		}

		return UserDto.from(user, tokenService.issue(user.getId()));
	}
}
