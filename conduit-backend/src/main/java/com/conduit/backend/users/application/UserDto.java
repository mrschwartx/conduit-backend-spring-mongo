package com.conduit.backend.users.application;

import com.conduit.backend.users.domain.User;

public record UserDto(String email, String token, String username, String bio, String image) {

	public static UserDto from(User user, String token) {
		return new UserDto(
				user.getEmail().value(),
				token,
				user.getUsername().value(),
				user.getBio().value(),
				user.getImage().value());
	}
}
