package com.conduit.backend.integration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

final class ApiClient {

	private final String baseUrl;

	private final String token;

	private final HttpClient http = HttpClient.newHttpClient();

	ApiClient(String baseUrl) {
		this(baseUrl, null);
	}

	private ApiClient(String baseUrl, String token) {
		this.baseUrl = baseUrl;
		this.token = token;
	}

	ApiClient withToken(String token) {
		return new ApiClient(baseUrl, token);
	}

	ApiResponse get(String path) {
		return send("GET", path, null, Map.of());
	}

	ApiResponse get(String path, Map<String, String> headers) {
		return send("GET", path, null, headers);
	}

	ApiResponse post(String path, String json) {
		return send("POST", path, json, Map.of());
	}

	ApiResponse put(String path, String json) {
		return send("PUT", path, json, Map.of());
	}

	ApiResponse delete(String path) {
		return send("DELETE", path, null, Map.of());
	}

	ApiResponse options(String path, Map<String, String> headers) {
		return send("OPTIONS", path, null, headers);
	}

	ApiResponse send(String method, String path, String body, Map<String, String> extraHeaders) {
		Map<String, String> headers = new HashMap<>(extraHeaders);
		if (token != null) {
			headers.putIfAbsent("Authorization", "Token " + token);
		}
		if (body != null) {
			headers.putIfAbsent("Content-Type", "application/json");
		}
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path));
		headers.forEach(builder::header);
		builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body));
		try {
			HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
			return new ApiResponse(response.statusCode(), response.body(), response.headers());
		}
		catch (IOException error) {
			throw new IllegalStateException(error);
		}
		catch (InterruptedException error) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(error);
		}
	}
}
