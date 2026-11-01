package com.conduit.backend.shared.domain;

public record Profile(String username, String bio, String image, boolean following) {
}
