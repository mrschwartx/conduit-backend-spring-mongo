package com.conduit.backend.users.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

	UUID nextId();

	void add(User user);

	boolean update(User user);

	Optional<User> findById(UUID id);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	boolean existsByEmail(String email, UUID excludeId);

	boolean existsByUsername(String username, UUID excludeId);
}
