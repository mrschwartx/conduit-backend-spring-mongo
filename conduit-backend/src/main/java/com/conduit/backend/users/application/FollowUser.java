package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.domain.ValidationException;
import com.conduit.backend.users.domain.FollowRepository;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FollowUser {

	private final UserRepository userRepository;

	private final FollowRepository followRepository;

	public FollowUser(UserRepository userRepository, FollowRepository followRepository) {
		this.userRepository = userRepository;
		this.followRepository = followRepository;
	}

	public Profile execute(UUID followerId, String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("profile"));
		if (user.getId().equals(followerId)) {
			throw new ValidationException("username", "you cannot follow yourself");
		}
		followRepository.follow(followerId, user.getId());
		return ProfileAssembler.of(user, true);
	}
}
