package com.conduit.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UsersIT extends IntegrationTestSupport {

	@Test
	void registersAUserAndReturnsAToken() {
		ApiResponse response = client().post("/api/v1/users", userBody("signup_user", "signup@mail.com", "Password123!"));

		assertThat(response.status()).isEqualTo(201);
		assertThat((String) response.json("$.user.username")).isEqualTo("signup_user");
		assertThat((String) response.json("$.user.email")).isEqualTo("signup@mail.com");
		assertThat((String) response.json("$.user.token")).contains(".");
		assertThat(response.<Object>json("$.user.bio")).isNull();
		assertThat(response.<Object>json("$.user.image")).isNull();
		assertThat(response.body()).doesNotContain("password");
	}

	@Test
	void registrationConflictsReturn409() {
		Registered existing = register();
		String other = UUID.randomUUID().toString().substring(0, 6);

		ApiResponse sameEmail = client().post("/api/v1/users",
				userBody("other_" + other, existing.email().toUpperCase(), "Password123!"));
		ApiResponse sameUsername = client().post("/api/v1/users",
				userBody(existing.username(), other + "@mail.com", "Password123!"));

		assertThat(sameEmail.status()).isEqualTo(409);
		assertThat((List<String>) sameEmail.json("$.errors.email")).containsExactly("has already been taken");
		assertThat(sameUsername.status()).isEqualTo(409);
		assertThat((List<String>) sameUsername.json("$.errors.username")).containsExactly("has already been taken");
	}

	@Test
	void oversizedBodiesReturn413() {
		String filler = "x".repeat(300 * 1024);

		ApiResponse response = client().post("/api/v1/users", userBody("big_user", "big@mail.com", filler));

		assertThat(response.status()).isEqualTo(413);
		assertThat((List<String>) response.json("$.errors.body")).containsExactly("is too large");
	}

	@Test
	void invalidRegistrationsReturn422() {
		ApiClient client = client();

		ApiResponse empty = client.post("/api/v1/users", "{}");
		ApiResponse shortName = client.post("/api/v1/users", userBody("ab", "a@mail.com", "Password123!"));
		ApiResponse badEmail = client.post("/api/v1/users", userBody("valid_user", "nope", "Password123!"));
		ApiResponse shortPassword = client.post("/api/v1/users", userBody("valid_user", "a@mail.com", "short"));

		assertThat(empty.status()).isEqualTo(422);
		assertThat((List<String>) empty.json("$.errors.user")).containsExactly("can't be blank");
		assertThat(client.post("/api/v1/users", "{\"user\":{}}").<Object>json("$.errors.username")).isNotNull();
		assertThat(shortName.status()).isEqualTo(422);
		assertThat((List<String>) shortName.json("$.errors.username"))
				.containsExactly("is too short (minimum is 3 characters)");
		assertThat(badEmail.status()).isEqualTo(422);
		assertThat((List<String>) badEmail.json("$.errors.email")).containsExactly("is invalid");
		assertThat(shortPassword.status()).isEqualTo(422);
		assertThat((List<String>) shortPassword.json("$.errors.password"))
				.containsExactly("is too short (minimum is 8 characters)");
	}

	@Test
	void logsInWithValidCredentialsOnly() {
		Registered user = register();

		ApiResponse ok = client().post("/api/v1/users/login",
				"{\"user\":{\"email\":\"" + user.email() + "\",\"password\":\"" + user.password() + "\"}}");
		ApiResponse wrong = client().post("/api/v1/users/login",
				"{\"user\":{\"email\":\"" + user.email() + "\",\"password\":\"definitely-wrong\"}}");
		ApiResponse unknown = client().post("/api/v1/users/login",
				"{\"user\":{\"email\":\"ghost@mail.com\",\"password\":\"Password123!\"}}");
		ApiResponse missing = client().post("/api/v1/users/login", "{\"user\":{\"email\":\"a@mail.com\"}}");
		ApiResponse injection = client().post("/api/v1/users/login",
				"{\"user\":{\"email\":{\"$ne\":null},\"password\":\"Password123!\"}}");

		assertThat(ok.status()).isEqualTo(200);
		assertThat((String) ok.json("$.user.username")).isEqualTo(user.username());
		assertThat(wrong.status()).isEqualTo(401);
		assertThat((List<String>) wrong.json("$.errors.credentials")).containsExactly("are invalid");
		assertThat(unknown.status()).isEqualTo(401);
		assertThat(missing.status()).isEqualTo(422);
		assertThat(injection.status()).isEqualTo(422);
	}

	@Test
	void guardsProtectedRoutes() {
		ApiResponse missing = client().get("/api/v1/user");
		ApiResponse invalid = client().get("/api/v1/user", Map.of("Authorization", "Token garbage"));
		ApiResponse wrongScheme = client().get("/api/v1/user", Map.of("Authorization", "Bearer abc"));

		assertThat(missing.status()).isEqualTo(401);
		assertThat((List<String>) missing.json("$.errors.token")).containsExactly("is missing");
		assertThat(invalid.status()).isEqualTo(401);
		assertThat((List<String>) invalid.json("$.errors.token")).containsExactly("is invalid");
		assertThat(wrongScheme.status()).isEqualTo(401);
		assertThat(client().get("/api/v1/articles", Map.of("Authorization", "Token garbage")).status()).isEqualTo(401);
		assertThat(client().get("/api/v1/tags", Map.of("Authorization", "Token garbage")).status()).isEqualTo(401);
		assertThat(client().put("/api/v1/user", "{\"user\":{\"bio\":\"x\"}}").status()).isEqualTo(401);
		assertThat(client().post("/api/v1/articles", articleBody("Anything")).status()).isEqualTo(401);
		assertThat(client().get("/api/v1/articles/feed").status()).isEqualTo(401);
		assertThat(client().post("/api/v1/profiles/someone/follow", "").status()).isEqualTo(401);
		assertThat(client().post("/api/v1/articles/some-slug/favorite", "").status()).isEqualTo(401);
	}

	@Test
	void getsTheCurrentUser() {
		Registered user = register();

		ApiResponse response = user.client().get("/api/v1/user");

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.user.username")).isEqualTo(user.username());
		assertThat((String) response.json("$.user.email")).isEqualTo(user.email());
		assertThat((String) response.json("$.user.token")).isNotBlank();
	}

	@Test
	void updatesTheCurrentUser() {
		Registered user = register();
		String renamed = "renamed_" + System.nanoTime();

		ApiResponse response = user.client().put("/api/v1/user", "{\"user\":{\"username\":\"" + renamed
				+ "\",\"bio\":\"I like turtles\",\"image\":\"https://cdn.example.com/me.png\","
				+ "\"password\":\"BrandNewPassword1\"}}");

		assertThat(response.status()).isEqualTo(200);
		assertThat((String) response.json("$.user.username")).isEqualTo(renamed);
		assertThat((String) response.json("$.user.bio")).isEqualTo("I like turtles");
		assertThat((String) response.json("$.user.image")).isEqualTo("https://cdn.example.com/me.png");
		assertThat(client().post("/api/v1/users/login", "{\"user\":{\"email\":\"" + user.email()
				+ "\",\"password\":\"BrandNewPassword1\"}}").status()).isEqualTo(200);
		assertThat(client().post("/api/v1/users/login", "{\"user\":{\"email\":\"" + user.email()
				+ "\",\"password\":\"" + user.password() + "\"}}").status()).isEqualTo(401);
	}

	@Test
	void updateCanClearBioAndImage() {
		Registered user = register();
		user.client().put("/api/v1/user", "{\"user\":{\"bio\":\"hello\",\"image\":\"https://a.com/x.png\"}}");

		ApiResponse response = user.client().put("/api/v1/user", "{\"user\":{\"bio\":null,\"image\":null}}");

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.<Object>json("$.user.bio")).isNull();
		assertThat(response.<Object>json("$.user.image")).isNull();
	}

	@Test
	void updateRejectsCollisionsAndEmptyChanges() {
		Registered first = register();
		Registered second = register();

		ApiResponse collision = second.client().put("/api/v1/user",
				"{\"user\":{\"username\":\"" + first.username() + "\"}}");
		ApiResponse empty = second.client().put("/api/v1/user", "{\"user\":{}}");
		ApiResponse badImage = second.client().put("/api/v1/user", "{\"user\":{\"image\":\"http://a.com/x.png\"}}");

		assertThat(collision.status()).isEqualTo(409);
		assertThat((List<String>) collision.json("$.errors.username")).containsExactly("has already been taken");
		assertThat(empty.status()).isEqualTo(422);
		assertThat((List<String>) empty.json("$.errors.user")).containsExactly("can't be empty");
		assertThat(badImage.status()).isEqualTo(422);
	}

	@Test
	void profilesCanBeFollowedAndUnfollowed() {
		Registered follower = register();
		Registered author = register();
		String path = "/api/v1/profiles/" + author.username();

		ApiResponse anonymous = client().get(path);
		ApiResponse follow = follower.client().post(path + "/follow", "");
		ApiResponse afterFollow = follower.client().get(path);
		ApiResponse unfollow = follower.client().delete(path + "/follow");

		assertThat(anonymous.status()).isEqualTo(200);
		assertThat((String) anonymous.json("$.profile.username")).isEqualTo(author.username());
		assertThat(anonymous.<Boolean>json("$.profile.following")).isFalse();
		assertThat(follow.status()).isEqualTo(200);
		assertThat(follow.<Boolean>json("$.profile.following")).isTrue();
		assertThat(afterFollow.<Boolean>json("$.profile.following")).isTrue();
		assertThat(unfollow.<Boolean>json("$.profile.following")).isFalse();
	}

	@Test
	void profileErrorsAreReported() {
		Registered user = register();

		ApiResponse unknown = client().get("/api/v1/profiles/nobody_here");
		ApiResponse followUnknown = user.client().post("/api/v1/profiles/nobody_here/follow", "");
		ApiResponse followSelf = user.client().post("/api/v1/profiles/" + user.username() + "/follow", "");

		assertThat(unknown.status()).isEqualTo(404);
		assertThat((List<String>) unknown.json("$.errors.profile")).containsExactly("not found");
		assertThat(followUnknown.status()).isEqualTo(404);
		assertThat(followSelf.status()).isEqualTo(422);
		assertThat((List<String>) followSelf.json("$.errors.username")).containsExactly("you cannot follow yourself");
	}
}
