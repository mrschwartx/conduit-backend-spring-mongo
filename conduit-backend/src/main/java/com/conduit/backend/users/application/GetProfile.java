package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.users.domain.FollowRepository;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetProfile {

	private final UserRepository userRepository;

	private final FollowRepository followRepository;

	public GetProfile(UserRepository userRepository, FollowRepository followRepository) {
		this.userRepository = userRepository;
		this.followRepository = followRepository;
	}

	public Profile execute(UUID viewerId, String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("profile"));
		boolean following = viewerId != null && followRepository.isFollowing(viewerId, user.getId());
		return ProfileAssembler.of(user, following);
	}
}
