package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Validation;
import com.conduit.backend.shared.domain.ValidationException;
import com.conduit.backend.users.domain.Bio;
import com.conduit.backend.users.domain.Email;
import com.conduit.backend.users.domain.ImageUrl;
import com.conduit.backend.users.domain.PasswordHasher;
import com.conduit.backend.users.domain.PlainPassword;
import com.conduit.backend.users.domain.TokenService;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserChanges;
import com.conduit.backend.users.domain.UserRepository;
import com.conduit.backend.users.domain.Username;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateCurrentUser {

	private static final List<String> FIELDS = List.of("username", "email", "password", "bio", "image");

	private final UserRepository userRepository;

	private final PasswordHasher passwordHasher;

	private final TokenService tokenService;

	public UpdateCurrentUser(UserRepository userRepository, PasswordHasher passwordHasher,
			TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
	}

	public UserDto execute(UUID userId, Map<String, Object> input) {
		if (!FIELDS.stream().anyMatch(input::containsKey)) {
			throw new ValidationException("user", "can't be empty");
		}

		Validation validation = new Validation();
		Username username = input.containsKey("username")
				? validation.check(() -> Username.create(input.get("username"))) : null;
		Email email = input.containsKey("email") ? validation.check(() -> Email.create(input.get("email"))) : null;
		PlainPassword password = input.containsKey("password")
				? validation.check(() -> PlainPassword.create(input.get("password"))) : null;
		Bio bio = input.containsKey("bio") ? validation.check(() -> Bio.create(input.get("bio"))) : null;
		ImageUrl image = input.containsKey("image")
				? validation.check(() -> ImageUrl.create(input.get("image"))) : null;
		validation.failIfAny();

		User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user"));

		List<String> taken = new ArrayList<>();
		if (username != null && userRepository.existsByUsername(username.value(), userId)) {
			taken.add("username");
		}
		if (email != null && userRepository.existsByEmail(email.value(), userId)) {
			taken.add("email");
		}
		if (!taken.isEmpty()) {
			throw new AlreadyExistsException(taken.toArray(String[]::new));
		}

		String passwordHash = password != null ? passwordHasher.hash(password.value()) : null;
		user.update(new UserChanges(username, email, passwordHash, bio, image));

		if (!userRepository.update(user)) {
			throw new NotFoundException("user");
		}
		return UserDto.from(user, tokenService.issue(user.getId()));
	}
}
