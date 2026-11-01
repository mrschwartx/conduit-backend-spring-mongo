package com.conduit.backend.users.presentation;

import com.conduit.backend.shared.application.AuthenticatedUser;
import com.conduit.backend.shared.infrastructure.http.RequestBodies;
import com.conduit.backend.users.application.GetCurrentUser;
import com.conduit.backend.users.application.UpdateCurrentUser;
import com.conduit.backend.users.application.UserDto;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class CurrentUserController {

	private final GetCurrentUser getCurrentUser;

	private final UpdateCurrentUser updateCurrentUser;

	public CurrentUserController(GetCurrentUser getCurrentUser, UpdateCurrentUser updateCurrentUser) {
		this.getCurrentUser = getCurrentUser;
		this.updateCurrentUser = updateCurrentUser;
	}

	@GetMapping
	public Map<String, UserDto> current(@AuthenticationPrincipal AuthenticatedUser requester) {
		return Map.of("user", getCurrentUser.execute(requester.userId()));
	}

	@PutMapping
	public Map<String, UserDto> update(@AuthenticationPrincipal AuthenticatedUser requester,
			@RequestBody(required = false) Map<String, Object> body) {
		return Map.of("user", updateCurrentUser.execute(requester.userId(), RequestBodies.section(body, "user")));
	}
}
