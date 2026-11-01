package com.conduit.backend.shared.infrastructure.http;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	@GetMapping("/")
	public Map<String, String> welcome() {
		return Map.of("message", "Welcome to the Conduit API");
	}

	@GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
	public String health() {
		return "OK";
	}
}
