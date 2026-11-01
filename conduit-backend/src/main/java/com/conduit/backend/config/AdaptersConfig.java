package com.conduit.backend.config;

import com.conduit.backend.users.domain.PasswordHasher;
import com.conduit.backend.users.domain.TokenService;
import com.conduit.backend.users.infrastructure.BcryptPasswordHasher;
import com.conduit.backend.users.infrastructure.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdaptersConfig {

	@Bean
	PasswordHasher passwordHasher(ConduitProperties properties) {
		return new BcryptPasswordHasher(properties.auth().bcryptRounds());
	}

	@Bean
	TokenService tokenService(ConduitProperties properties) {
		return new JwtTokenService(properties.auth().secret(), properties.auth().tokenTtl());
	}
}
