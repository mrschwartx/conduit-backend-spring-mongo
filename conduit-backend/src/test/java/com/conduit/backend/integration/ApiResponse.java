package com.conduit.backend.integration;

import com.jayway.jsonpath.JsonPath;
import java.net.http.HttpHeaders;

record ApiResponse(int status, String body, HttpHeaders headers) {

	<T> T json(String path) {
		return JsonPath.read(body, path);
	}

	String header(String name) {
		return headers.firstValue(name).orElse(null);
	}
}
