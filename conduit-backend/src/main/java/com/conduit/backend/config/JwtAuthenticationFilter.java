package com.conduit.backend.config;

import com.conduit.backend.shared.application.AuthenticatedUser;
import com.conduit.backend.shared.domain.DomainException;
import com.conduit.backend.users.application.VerifyAccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String API_PREFIX = "/api/v1";

	private static final String SCHEME = "token ";

	private final VerifyAccessToken verifyAccessToken;

	private final HandlerExceptionResolver exceptionResolver;

	public JwtAuthenticationFilter(VerifyAccessToken verifyAccessToken, HandlerExceptionResolver exceptionResolver) {
		this.verifyAccessToken = verifyAccessToken;
		this.exceptionResolver = exceptionResolver;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return !(path.equals(API_PREFIX) || path.startsWith(API_PREFIX + "/"));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || header.isBlank()) {
			chain.doFilter(request, response);
			return;
		}
		try {
			AuthenticatedUser user = verifyAccessToken.execute(readToken(header));
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, List.of()));
			SecurityContextHolder.setContext(context);
		}
		catch (DomainException error) {
			SecurityContextHolder.clearContext();
			exceptionResolver.resolveException(request, response, null, error);
			return;
		}
		chain.doFilter(request, response);
	}

	private String readToken(String header) {
		String value = header.trim();
		if (value.toLowerCase(Locale.ROOT).startsWith(SCHEME)) {
			return value.substring(SCHEME.length()).trim();
		}
		return value;
	}
}
