package com.conduit.backend.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.shared.domain.ValidationException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValueObjectsTest {

	private static void assertRejected(Runnable action, String field, String message) {
		assertThatThrownBy(action::run)
				.isInstanceOf(ValidationException.class)
				.extracting("errors")
				.isEqualTo(Map.of(field, List.of(message)));
	}

	@Test
	void emailIsTrimmedAndValidated() {
		assertThat(Email.create("  John@Mail.com ").value()).isEqualTo("john@mail.com");
		assertRejected(() -> Email.create("nope"), "email", "is invalid");
		assertRejected(() -> Email.create("a b@mail.com"), "email", "is invalid");
		assertRejected(() -> Email.create(null), "email", "can't be blank");
		assertRejected(() -> Email.create("   "), "email", "can't be blank");
		assertRejected(() -> Email.create("a".repeat(250) + "@mail.com"), "email", "is invalid");
	}

	@Test
	void usernameLengthIsEnforcedAfterTrimming() {
		assertThat(Username.create("  john ").value()).isEqualTo("john");
		assertRejected(() -> Username.create("ab"), "username", "is too short (minimum is 3 characters)");
		assertRejected(() -> Username.create("a".repeat(31)), "username", "is too long (maximum is 30 characters)");
		assertRejected(() -> Username.create("john doe"), "username", "is invalid");
		assertRejected(() -> Username.create("john@doe"), "username", "is invalid");
		assertThat(Username.create("john_doe-1").value()).isEqualTo("john_doe-1");
		assertRejected(() -> Username.create(12), "username", "can't be blank");
	}

	@Test
	void passwordRulesCountCharactersAndBytes() {
		assertThat(PlainPassword.create("Password123!").value()).isEqualTo("Password123!");
		assertRejected(() -> PlainPassword.create("short"), "password", "is too short (minimum is 8 characters)");
		assertRejected(() -> PlainPassword.create("é".repeat(37)), "password", "is too long (maximum is 72 characters)");
		assertRejected(() -> PlainPassword.create(null), "password", "can't be blank");
		assertThat(PlainPassword.create("Password123!").toString()).doesNotContain("Password123!");
	}

	@Test
	void bioAllowsNullAndBlankAsEmpty() {
		assertThat(Bio.create(null).value()).isNull();
		assertThat(Bio.create("   ").value()).isNull();
		assertThat(Bio.create(" hello ").value()).isEqualTo("hello");
		assertRejected(() -> Bio.create(5), "bio", "is invalid");
		assertRejected(() -> Bio.create("a".repeat(301)), "bio", "is too long (maximum is 300 characters)");
	}

	@Test
	void imageMustBeAWebAddress() {
		assertThat(ImageUrl.create(null).value()).isNull();
		assertThat(ImageUrl.create("").value()).isNull();
		assertThat(ImageUrl.create("https://cdn.example.com/me.png").value()).isEqualTo("https://cdn.example.com/me.png");
		assertRejected(() -> ImageUrl.create("javascript:alert(1)"), "image", "is invalid");
		assertRejected(() -> ImageUrl.create("not a url"), "image", "is invalid");
		assertRejected(() -> ImageUrl.create("http://cdn.example.com/me.png"), "image", "is invalid");
		assertRejected(() -> ImageUrl.create(12), "image", "is invalid");
	}
}
