package com.conduit.backend.comments.domain;

import java.util.Optional;
import java.util.UUID;

public interface ArticleLookup {

	Optional<UUID> findIdBySlug(String slug);
}
