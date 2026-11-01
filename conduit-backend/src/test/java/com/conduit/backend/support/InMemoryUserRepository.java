package com.conduit.backend.support;

import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {

	private final Map<UUID, User> users = new LinkedHashMap<>();

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	public void add(User user) {
		if (existsByEmail(user.getEmail().value(), null)) {
			throw new AlreadyExistsException("email");
		}
		if (existsByUsername(user.getUsername().value(), null)) {
			throw new AlreadyExistsException("username");
		}
		users.put(user.getId(), user);
	}

	@Override
	public boolean update(User user) {
		if (!users.containsKey(user.getId())) {
			return false;
		}
		users.put(user.getId(), user);
		return true;
	}

	@Override
	public Optional<User> findById(UUID id) {
		return Optional.ofNullable(users.get(id));
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return users.values().stream().filter(user -> user.getEmail().value().equalsIgnoreCase(email)).findFirst();
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return users.values().stream()
				.filter(user -> user.getUsername().value().equalsIgnoreCase(username))
				.findFirst();
	}

	@Override
	public boolean existsByEmail(String email, UUID excludeId) {
		return users.values().stream()
				.filter(user -> !user.getId().equals(excludeId))
				.anyMatch(user -> user.getEmail().value().equalsIgnoreCase(email));
	}

	@Override
	public boolean existsByUsername(String username, UUID excludeId) {
		return users.values().stream()
				.filter(user -> !user.getId().equals(excludeId))
				.anyMatch(user -> user.getUsername().value().equalsIgnoreCase(username));
	}
}
