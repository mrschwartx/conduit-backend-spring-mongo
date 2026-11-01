package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.users.domain.TokenService;
import com.conduit.backend.users.domain.User;
import com.conduit.backend.users.domain.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUser {

	private final UserRepository userRepository;

	private final TokenService tokenService;

	public GetCurrentUser(UserRepository userRepository, TokenService tokenService) {
		this.userRepository = userRepository;
		this.tokenService = tokenService;
	}

	public UserDto execute(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user"));
		return UserDto.from(user, tokenService.issue(user.getId()));
	}
}
