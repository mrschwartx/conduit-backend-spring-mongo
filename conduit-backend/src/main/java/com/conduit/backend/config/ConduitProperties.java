package com.conduit.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "conduit")
public record ConduitProperties(
		@Valid @NotNull App app,
		@NotBlank String clientUrl,
		@Valid @NotNull Auth auth,
		@Valid @NotNull Seed seed) {

	public record App(@NotBlank String level) {

		public boolean production() {
			return "production".equals(level);
		}
	}

	public record Auth(
			@NotBlank(message = "AUTH_SECRET is required")
			@Size(min = 32, message = "AUTH_SECRET must be at least 32 characters long") String secret,
			@NotNull Duration tokenTtl,
			@Min(4) @Max(31) int bcryptRounds) {
	}

	public record Seed(boolean enabled, boolean allowProduction) {
	}
}
