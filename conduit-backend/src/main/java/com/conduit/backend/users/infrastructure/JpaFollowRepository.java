package com.conduit.backend.users.infrastructure;

import com.conduit.backend.users.domain.FollowRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaFollowRepository implements FollowRepository {

	private final EntityManager entityManager;

	public JpaFollowRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public void follow(UUID followerId, UUID followeeId) {
		entityManager.createNativeQuery(
				"insert into follows (follower_id, followee_id) values (:follower, :followee) on conflict do nothing")
				.setParameter("follower", followerId)
				.setParameter("followee", followeeId)
				.executeUpdate();
	}

	@Override
	@Transactional
	public void unfollow(UUID followerId, UUID followeeId) {
		entityManager.createNativeQuery(
				"delete from follows where follower_id = :follower and followee_id = :followee")
				.setParameter("follower", followerId)
				.setParameter("followee", followeeId)
				.executeUpdate();
	}

	@Override
	public boolean isFollowing(UUID followerId, UUID followeeId) {
		Number count = (Number) entityManager.createNativeQuery(
				"select count(*) from follows where follower_id = :follower and followee_id = :followee")
				.setParameter("follower", followerId)
				.setParameter("followee", followeeId)
				.getSingleResult();
		return count.longValue() > 0;
	}
}
