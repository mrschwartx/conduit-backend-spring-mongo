package com.conduit.backend.shared.infrastructure.http;

import java.io.IOException;

public class RequestTooLargeException extends IOException {

	public RequestTooLargeException() {
		super("Request body is too large");
	}

}
