package com.conduit.backend.shared.infrastructure.http;

import java.util.List;
import java.util.Map;

public record ErrorBody(Map<String, List<String>> errors) {
}
