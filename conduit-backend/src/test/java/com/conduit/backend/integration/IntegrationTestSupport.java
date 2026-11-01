package com.conduit.backend.integration;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"logging.file.name=",
		"conduit.auth.secret=integration-test-secret-integration-test-secret",
		"conduit.auth.bcrypt-rounds=4" })
abstract class IntegrationTestSupport {

	@Value("${local.server.port}")
	private int port;

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		TestDatabase database = TestDatabase.INSTANCE;
		registry.add("spring.datasource.url", () -> database.jdbcUrl);
		registry.add("spring.datasource.username", () -> database.username);
		registry.add("spring.datasource.password", () -> database.password);
	}

	ApiClient client() {
		return new ApiClient("http://127.0.0.1:" + port);
	}

	record Registered(ApiClient client, String username, String email, String password, String token) {
	}

	Registered register() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String username = "user_" + suffix;
		String email = suffix + "@mail.com";
		String password = "Password123!";
		ApiResponse response = client().post("/api/v1/users", userBody(username, email, password));
		if (response.status() != 201) {
			throw new IllegalStateException("Registration failed: " + response.body());
		}
		String token = response.json("$.user.token");
		return new Registered(client().withToken(token), username, email, password, token);
	}

	static String userBody(String username, String email, String password) {
		return "{\"user\":{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\""
				+ password + "\"}}";
	}

	static String articleBody(String title, String... tags) {
		StringBuilder list = new StringBuilder();
		for (String tag : tags) {
			if (list.length() > 0) {
				list.append(',');
			}
			list.append('"').append(tag).append('"');
		}
		return "{\"article\":{\"title\":\"" + title + "\",\"description\":\"About it\",\"body\":\"The body\","
				+ "\"tagList\":[" + list + "]}}";
	}

	String uniqueTitle(String prefix) {
		return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
	}

	String slugOf(String title) {
		return title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
	}
}
