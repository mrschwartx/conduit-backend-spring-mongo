package com.conduit.backend.users.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

	@Query("select u from UserEntity u where lower(u.email) = lower(:email)")
	Optional<UserEntity> findByEmailIgnoringCase(@Param("email") String email);

	@Query("select u from UserEntity u where lower(u.username) = lower(:username)")
	Optional<UserEntity> findByUsernameIgnoringCase(@Param("username") String username);

	@Query("select count(u) from UserEntity u where lower(u.email) = lower(:email) and u.id <> :excludeId")
	long countByEmailExcluding(@Param("email") String email, @Param("excludeId") UUID excludeId);

	@Query("select count(u) from UserEntity u where lower(u.username) = lower(:username) and u.id <> :excludeId")
	long countByUsernameExcluding(@Param("username") String username, @Param("excludeId") UUID excludeId);
}
