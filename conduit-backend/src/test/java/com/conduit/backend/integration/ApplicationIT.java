package com.conduit.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApplicationIT extends IntegrationTestSupport {

	@Test
	void greetsOnTheRootPath() {
		ApiResponse response = client().get("/");

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.message")).isEqualTo("Welcome to the Conduit API");
	}

	@Test
	void respondsWith404ForUnknownRoutes() {
		ApiResponse response = client().get("/api/v1/unknown");

		assertThat(response.status()).isEqualTo(404);
		assertThat((List<String>) response.json("$.errors.path")).containsExactly("not found");
	}

	@Test
	void respondsWith400ForMalformedJson() {
		ApiResponse response = client().post("/api/v1/users/login", "{broken");

		assertThat(response.status()).isEqualTo(400);
		assertThat((List<String>) response.json("$.errors.body")).containsExactly("is not valid JSON");
	}

	@Test
	void allowsTheConfiguredClientOriginAndAuthorizationHeader() {
		ApiResponse response = client().options("/api/v1/user", Map.of(
				"Origin", "http://localhost:3000",
				"Access-Control-Request-Method", "PUT",
				"Access-Control-Request-Headers", "authorization,content-type"));

		assertThat(response.header("access-control-allow-origin")).isEqualTo("http://localhost:3000");
		assertThat(response.header("access-control-allow-headers")).containsIgnoringCase("authorization");
	}

	@Test
	void rejectsBodiesThatAreNotJson() {
		ApiResponse response = client().send("POST", "/api/v1/users/login", "user=1", Map.of("Content-Type", "text/plain"));

		assertThat(response.status()).isEqualTo(415);
		assertThat((List<String>) response.json("$.errors['content-type']")).containsExactly("must be application/json");
	}

	@Test
	void exposesTheHealthEndpoint() {
		ApiResponse response = client().get("/health");

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.body()).isEqualTo("OK");
	}
}
