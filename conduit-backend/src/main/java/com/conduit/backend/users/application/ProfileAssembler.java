package com.conduit.backend.users.application;

import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.users.domain.User;

final class ProfileAssembler {

	private ProfileAssembler() {
	}

	static Profile of(User user, boolean following) {
		return new Profile(user.getUsername().value(), user.getBio().value(), user.getImage().value(), following);
	}
}
