package com.conduit.backend.articles.domain;

import java.util.UUID;

public interface FavoriteRepository {

	void favorite(UUID userId, UUID articleId);

	void unfavorite(UUID userId, UUID articleId);
}
