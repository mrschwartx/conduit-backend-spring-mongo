package com.conduit.backend.users.presentation;

import com.conduit.backend.shared.infrastructure.http.RequestBodies;
import com.conduit.backend.users.application.AuthenticateUser;
import com.conduit.backend.users.application.RegisterUser;
import com.conduit.backend.users.application.UserDto;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {

	private final RegisterUser registerUser;

	private final AuthenticateUser authenticateUser;

	public AuthController(RegisterUser registerUser, AuthenticateUser authenticateUser) {
		this.registerUser = registerUser;
		this.authenticateUser = authenticateUser;
	}

	@PostMapping
	public ResponseEntity<Map<String, UserDto>> register(@RequestBody(required = false) Map<String, Object> body) {
		UserDto user = registerUser.execute(RequestBodies.section(body, "user"));
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("user", user));
	}

	@PostMapping("/login")
	public Map<String, UserDto> login(@RequestBody(required = false) Map<String, Object> body) {
		return Map.of("user", authenticateUser.execute(RequestBodies.section(body, "user")));
	}
}
