package com.conduit.backend.shared.infrastructure.http;

import com.conduit.backend.shared.domain.DomainException;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static final Map<String, HttpStatus> STATUS_BY_CODE = Map.of(
			"VALIDATION_ERROR", HttpStatus.UNPROCESSABLE_CONTENT,
			"NOT_FOUND", HttpStatus.NOT_FOUND,
			"ALREADY_EXISTS", HttpStatus.CONFLICT,
			"AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED,
			"FORBIDDEN", HttpStatus.FORBIDDEN);

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorBody> handleDomain(DomainException error) {
		HttpStatus status = STATUS_BY_CODE.getOrDefault(error.getCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		log.warn("Request rejected status={} errors={}", status.value(), error.getErrors());
		return ResponseEntity.status(status).body(new ErrorBody(error.getErrors()));
	}

	@ExceptionHandler(RequestTooLargeException.class)
	public ResponseEntity<ErrorBody> handleTooLarge(RequestTooLargeException error) {
		return reject(HttpStatus.CONTENT_TOO_LARGE, "body", "is too large");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorBody> handleUnreadable(HttpMessageNotReadableException error) {
		for (Throwable cause = error.getCause(); cause != null; cause = cause.getCause()) {
			if (cause instanceof RequestTooLargeException) {
				return reject(HttpStatus.CONTENT_TOO_LARGE, "body", "is too large");
			}
		}
		return reject(HttpStatus.BAD_REQUEST, "body", "is not valid JSON");
	}

	@ExceptionHandler(ServletException.class)
	public ResponseEntity<ErrorBody> handleServlet(ServletException error) {
		if (error instanceof ErrorResponse errorResponse) {
			HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
			if (status == HttpStatus.NOT_FOUND) {
				return reject(status, "path", "not found");
			}
			if (status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
				return reject(status, "content-type", "must be application/json");
			}
			if (status.is4xxClientError()) {
				return reject(status, "request", errorResponse.getBody().getDetail());
			}
		}
		return handleUnexpected(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorBody> handleUnexpected(Exception error) {
		log.error("Unhandled error: {}", error.getMessage(), error);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorBody(Map.of("server", List.of("internal server error"))));
	}

	private ResponseEntity<ErrorBody> reject(HttpStatus status, String field, String message) {
		log.warn("Request rejected status={} field={} message={}", status.value(), field, message);
		return ResponseEntity.status(status).body(new ErrorBody(Map.of(field, List.of(message))));
	}
}
