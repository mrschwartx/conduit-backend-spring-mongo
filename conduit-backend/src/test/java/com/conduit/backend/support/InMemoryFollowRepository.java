package com.conduit.backend.support;

import com.conduit.backend.users.domain.FollowRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InMemoryFollowRepository implements FollowRepository {

	private final Set<List<UUID>> follows = new HashSet<>();

	@Override
	public void follow(UUID followerId, UUID followeeId) {
		follows.add(List.of(followerId, followeeId));
	}

	@Override
	public void unfollow(UUID followerId, UUID followeeId) {
		follows.remove(List.of(followerId, followeeId));
	}

	@Override
	public boolean isFollowing(UUID followerId, UUID followeeId) {
		return follows.contains(List.of(followerId, followeeId));
	}
}
