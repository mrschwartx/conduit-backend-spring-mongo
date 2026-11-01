package com.conduit.backend.users.presentation;

import com.conduit.backend.shared.application.AuthenticatedUser;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.users.application.FollowUser;
import com.conduit.backend.users.application.GetProfile;
import com.conduit.backend.users.application.UnfollowUser;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfilesController {

	private final GetProfile getProfile;

	private final FollowUser followUser;

	private final UnfollowUser unfollowUser;

	public ProfilesController(GetProfile getProfile, FollowUser followUser, UnfollowUser unfollowUser) {
		this.getProfile = getProfile;
		this.followUser = followUser;
		this.unfollowUser = unfollowUser;
	}

	@GetMapping("/{username}")
	public Map<String, Profile> get(@AuthenticationPrincipal AuthenticatedUser viewer,
			@PathVariable String username) {
		return Map.of("profile", getProfile.execute(viewerId(viewer), username));
	}

	@PostMapping("/{username}/follow")
	public Map<String, Profile> follow(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String username) {
		return Map.of("profile", followUser.execute(requester.userId(), username));
	}

	@DeleteMapping("/{username}/follow")
	public Map<String, Profile> unfollow(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String username) {
		return Map.of("profile", unfollowUser.execute(requester.userId(), username));
	}

	private static UUID viewerId(AuthenticatedUser viewer) {
		return viewer != null ? viewer.userId() : null;
	}
}
