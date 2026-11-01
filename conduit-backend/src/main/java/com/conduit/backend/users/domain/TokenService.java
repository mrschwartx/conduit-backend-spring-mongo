package com.conduit.backend.users.domain;

import java.util.UUID;

public interface TokenService {

	String issue(UUID userId);

	UUID verify(String token);
}
