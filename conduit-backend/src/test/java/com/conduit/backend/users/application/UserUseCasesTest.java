package com.conduit.backend.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.AuthenticationFailedException;
import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.domain.ValidationException;
import com.conduit.backend.support.FakePasswordHasher;
import com.conduit.backend.support.FakeTokenService;
import com.conduit.backend.support.InMemoryFollowRepository;
import com.conduit.backend.support.InMemoryUserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCasesTest {

	private InMemoryUserRepository users;

	private InMemoryFollowRepository follows;

	private FakePasswordHasher hasher;

	private FakeTokenService tokens;

	private RegisterUser registerUser;

	private AuthenticateUser authenticateUser;

	private UpdateCurrentUser updateCurrentUser;

	@BeforeEach
	void setUp() {
		users = new InMemoryUserRepository();
		follows = new InMemoryFollowRepository();
		hasher = new FakePasswordHasher();
		tokens = new FakeTokenService();
		registerUser = new RegisterUser(users, hasher, tokens);
		authenticateUser = new AuthenticateUser(users, hasher, tokens);
		updateCurrentUser = new UpdateCurrentUser(users, hasher, tokens);
	}

	private UserDto register(String username, String email) {
		return registerUser.execute(Map.of("username", username, "email", email, "password", "password123"));
	}

	private UUID idOf(UserDto user) {
		return tokens.verify(user.token());
	}

	@Test
	void registersAUserAndIssuesAToken() {
		UserDto user = register("john", "john@mail.com");

		assertThat(user.username()).isEqualTo("john");
		assertThat(user.email()).isEqualTo("john@mail.com");
		assertThat(user.bio()).isNull();
		assertThat(user.image()).isNull();
		assertThat(user.token()).startsWith("token:");
	}

	@Test
	void registerReportsEveryTakenField() {
		register("john", "john@mail.com");

		assertThatThrownBy(() -> register("JOHN", "other@mail.com"))
				.isInstanceOf(AlreadyExistsException.class)
				.extracting("errors").isEqualTo(Map.of("username", List.of("has already been taken")));
		assertThatThrownBy(() -> register("other", "JOHN@mail.com"))
				.isInstanceOf(AlreadyExistsException.class)
				.extracting("errors").isEqualTo(Map.of("email", List.of("has already been taken")));
		assertThatThrownBy(() -> register("john", "john@mail.com"))
				.isInstanceOf(AlreadyExistsException.class)
				.extracting("errors")
				.isEqualTo(Map.of("username", List.of("has already been taken"),
						"email", List.of("has already been taken")));
	}

	@Test
	void registerValidatesInput() {
		assertThatThrownBy(() -> registerUser.execute(Map.of()))
				.isInstanceOf(ValidationException.class)
				.extracting("errors")
				.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
				.containsOnlyKeys("username", "email", "password");
		assertThatThrownBy(() -> registerUser.execute(
				Map.of("username", "john", "email", "john@mail.com", "password", "short")))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void logsInWithValidCredentials() {
		register("john", "john@mail.com");

		UserDto user = authenticateUser.execute(Map.of("email", "JOHN@mail.com", "password", "password123"));

		assertThat(user.username()).isEqualTo("john");
	}

	@Test
	void loginRejectsUnknownEmailAndWrongPasswordIdentically() {
		register("john", "john@mail.com");

		assertThatThrownBy(() -> authenticateUser.execute(Map.of("email", "ghost@mail.com", "password", "password123")))
				.isInstanceOf(AuthenticationFailedException.class)
				.extracting("errors").isEqualTo(Map.of("credentials", List.of("are invalid")));
		assertThatThrownBy(() -> authenticateUser.execute(Map.of("email", "john@mail.com", "password", "wrong-password")))
				.isInstanceOf(AuthenticationFailedException.class)
				.extracting("errors").isEqualTo(Map.of("credentials", List.of("are invalid")));
	}

	@Test
	void loginRequiresEmailAndPassword() {
		assertThatThrownBy(() -> authenticateUser.execute(Map.of("password", "password123")))
				.isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> authenticateUser.execute(Map.of("email", "john@mail.com")))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void getsTheCurrentUser() {
		UUID id = idOf(register("john", "john@mail.com"));
		GetCurrentUser getCurrentUser = new GetCurrentUser(users, tokens);

		assertThat(getCurrentUser.execute(id).username()).isEqualTo("john");
		assertThatThrownBy(() -> getCurrentUser.execute(UUID.randomUUID())).isInstanceOf(NotFoundException.class);
	}

	@Test
	void updatesProfileFieldsAndPassword() {
		UUID id = idOf(register("john", "john@mail.com"));
		Map<String, Object> changes = new HashMap<>();
		changes.put("username", "johnny");
		changes.put("bio", "I like turtles");
		changes.put("image", "https://cdn.example.com/me.png");
		changes.put("password", "newpassword1");

		UserDto updated = updateCurrentUser.execute(id, changes);

		assertThat(updated.username()).isEqualTo("johnny");
		assertThat(updated.bio()).isEqualTo("I like turtles");
		assertThat(updated.image()).isEqualTo("https://cdn.example.com/me.png");
		assertThat(authenticateUser.execute(Map.of("email", "john@mail.com", "password", "newpassword1"))
				.username()).isEqualTo("johnny");
	}

	@Test
	void updateCanClearBioAndImageWithNull() {
		UUID id = idOf(register("john", "john@mail.com"));
		updateCurrentUser.execute(id, Map.of("bio", "hello", "image", "https://cdn.example.com/me.png"));
		Map<String, Object> clear = new HashMap<>();
		clear.put("bio", null);
		clear.put("image", null);

		UserDto updated = updateCurrentUser.execute(id, clear);

		assertThat(updated.bio()).isNull();
		assertThat(updated.image()).isNull();
	}

	@Test
	void updateRejectsCollisionsAndEmptyChanges() {
		UUID id = idOf(register("john", "john@mail.com"));
		register("jane", "jane@mail.com");

		assertThatThrownBy(() -> updateCurrentUser.execute(id, Map.of("username", "jane")))
				.isInstanceOf(AlreadyExistsException.class);
		assertThatThrownBy(() -> updateCurrentUser.execute(id, Map.of("email", "jane@mail.com")))
				.isInstanceOf(AlreadyExistsException.class);
		assertThatThrownBy(() -> updateCurrentUser.execute(id, Map.of()))
				.isInstanceOf(ValidationException.class)
				.extracting("errors").isEqualTo(Map.of("user", List.of("can't be empty")));
		assertThat(updateCurrentUser.execute(id, Map.of("username", "JOHN")).username()).isEqualTo("JOHN");
	}

	@Test
	void followsAndUnfollowsUsers() {
		UUID john = idOf(register("john", "john@mail.com"));
		register("jane", "jane@mail.com");
		GetProfile getProfile = new GetProfile(users, follows);

		Profile followed = new FollowUser(users, follows).execute(john, "jane");

		assertThat(followed.following()).isTrue();
		assertThat(getProfile.execute(john, "jane").following()).isTrue();
		assertThat(getProfile.execute(null, "jane").following()).isFalse();

		Profile unfollowed = new UnfollowUser(users, follows).execute(john, "jane");

		assertThat(unfollowed.following()).isFalse();
		assertThat(getProfile.execute(john, "jane").following()).isFalse();
	}

	@Test
	void followRejectsSelfAndUnknownProfiles() {
		UUID john = idOf(register("john", "john@mail.com"));

		assertThatThrownBy(() -> new FollowUser(users, follows).execute(john, "john"))
				.isInstanceOf(ValidationException.class)
				.extracting("errors").isEqualTo(Map.of("username", List.of("you cannot follow yourself")));
		assertThatThrownBy(() -> new FollowUser(users, follows).execute(john, "ghost"))
				.isInstanceOf(NotFoundException.class);
		assertThatThrownBy(() -> new GetProfile(users, follows).execute(null, "ghost"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void registerLowercasesTheEmail() {
		assertThat(register("john", "John@Mail.com").email()).isEqualTo("john@mail.com");
	}

	@Test
	void verifiesAccessTokens() {
		VerifyAccessToken verify = new VerifyAccessToken(tokens);
		UUID id = idOf(register("john", "john@mail.com"));

		assertThat(verify.execute(tokens.issue(id)).userId()).isEqualTo(id);
		assertThatThrownBy(() -> verify.execute(null)).isInstanceOf(AuthenticationFailedException.class)
				.extracting("errors").isEqualTo(Map.of("token", List.of("is missing")));
		assertThatThrownBy(() -> verify.execute("garbage")).isInstanceOf(AuthenticationFailedException.class)
				.extracting("errors").isEqualTo(Map.of("token", List.of("is invalid")));
	}
}
