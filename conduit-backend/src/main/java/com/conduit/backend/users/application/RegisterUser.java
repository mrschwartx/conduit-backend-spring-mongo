package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.Validation;
import com.conduit.backend.users.domain.Email;
import com.conduit.backend.users.domain.PasswordHasher;
import com.conduit.backend.users.domain.PlainPassword;
import com.conduit.backend.users.domain.TokenService;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import com.conduit.backend.users.domain.Username;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RegisterUser {

	private final UserRepository userRepository;

	private final PasswordHasher passwordHasher;

	private final TokenService tokenService;

	public RegisterUser(UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
	}

	public UserDto execute(Map<String, Object> input) {
		Validation validation = new Validation();
		Username username = validation.check(() -> Username.create(input.get("username")));
		Email email = validation.check(() -> Email.create(input.get("email")));
		PlainPassword password = validation.check(() -> PlainPassword.create(input.get("password")));
		validation.failIfAny();

		List<String> taken = new ArrayList<>();
		if (userRepository.existsByUsername(username.value(), null)) {
			taken.add("username");
		}
		if (userRepository.existsByEmail(email.value(), null)) {
			taken.add("email");
		}
		if (!taken.isEmpty()) {
			throw new AlreadyExistsException(taken.toArray(String[]::new));
		}

		String passwordHash = passwordHasher.hash(password.value());
		User user = User.register(userRepository.nextId(), username, email, passwordHash);
		userRepository.add(user);

		return UserDto.from(user, tokenService.issue(user.getId()));
	}
}
