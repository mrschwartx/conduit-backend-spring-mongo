package com.conduit.backend.users.infrastructure;

import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.shared.infrastructure.persistence.UniqueViolations;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepository implements UserRepository {

	private static final UUID NO_EXCLUSION = new UUID(0L, 0L);

	private final UserJpaRepository jpaRepository;

	private final EntityManager entityManager;

	public JpaUserRepository(UserJpaRepository jpaRepository, EntityManager entityManager) {
		this.jpaRepository = jpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	@Transactional
	public void add(User user) {
		try {
			entityManager.persist(UserEntity.from(user));
			entityManager.flush();
		}
		catch (RuntimeException error) {
			throw translate(error);
		}
	}

	@Override
	@Transactional
	public boolean update(User user) {
		UserEntity entity = entityManager.find(UserEntity.class, user.getId());
		if (entity == null) {
			return false;
		}
		try {
			entity.apply(user);
			entityManager.flush();
			return true;
		}
		catch (RuntimeException error) {
			throw translate(error);
		}
	}

	@Override
	public Optional<User> findById(UUID id) {
		return jpaRepository.findById(id).map(UserEntity::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpaRepository.findByEmailIgnoringCase(email).map(UserEntity::toDomain);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return jpaRepository.findByUsernameIgnoringCase(username).map(UserEntity::toDomain);
	}

	@Override
	public boolean existsByEmail(String email, UUID excludeId) {
		return jpaRepository.countByEmailExcluding(email, excludeId != null ? excludeId : NO_EXCLUSION) > 0;
	}

	@Override
	public boolean existsByUsername(String username, UUID excludeId) {
		return jpaRepository.countByUsernameExcluding(username, excludeId != null ? excludeId : NO_EXCLUSION) > 0;
	}

	private static RuntimeException translate(RuntimeException error) {
		if (UniqueViolations.violates(error, "users_email_unique")) {
			return new AlreadyExistsException("email");
		}
		if (UniqueViolations.violates(error, "users_username_unique")) {
			return new AlreadyExistsException("username");
		}
		if (UniqueViolations.isUniqueViolation(error)) {
			return new AlreadyExistsException("username", "email");
		}
		return error;
	}
}
