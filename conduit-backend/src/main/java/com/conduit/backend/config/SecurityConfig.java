package com.conduit.backend.config;

import com.conduit.backend.shared.domain.AuthenticationFailedException;
import com.conduit.backend.shared.infrastructure.http.RequestSizeLimitFilter;
import com.conduit.backend.users.application.VerifyAccessToken;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource,
			VerifyAccessToken verifyAccessToken,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) throws Exception {
		JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(verifyAccessToken, exceptionResolver);
		RequestSizeLimitFilter sizeLimitFilter = new RequestSizeLimitFilter(exceptionResolver);

		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/api/v1/user", "/api/v1/profiles/*/follow")
						.authenticated()
						.requestMatchers(HttpMethod.GET, "/api/v1/articles/feed")
						.authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/articles", "/api/v1/articles/*/comments")
						.authenticated()
						.requestMatchers(HttpMethod.PUT, "/api/v1/articles/*")
						.authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/v1/articles/*", "/api/v1/articles/*/comments/*")
						.authenticated()
						.requestMatchers("/api/v1/articles/*/favorite")
						.authenticated()
						.anyRequest()
						.permitAll())
				.exceptionHandling(handling -> handling.authenticationEntryPoint(
						(request, response, error) -> exceptionResolver.resolveException(request, response, null,
								new AuthenticationFailedException("token", "is missing"))))
				.addFilterAfter(sizeLimitFilter, CorsFilter.class)
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(ConduitProperties properties) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(properties.clientUrl()));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	UserDetailsService userDetailsService() {
		return username -> {
			throw new UsernameNotFoundException("Password authentication is not supported");
		};
	}
}
