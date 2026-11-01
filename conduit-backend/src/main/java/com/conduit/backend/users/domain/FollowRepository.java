package com.conduit.backend.users.domain;

import java.util.UUID;

public interface FollowRepository {

	void follow(UUID followerId, UUID followeeId);

	void unfollow(UUID followerId, UUID followeeId);

	boolean isFollowing(UUID followerId, UUID followeeId);
}
