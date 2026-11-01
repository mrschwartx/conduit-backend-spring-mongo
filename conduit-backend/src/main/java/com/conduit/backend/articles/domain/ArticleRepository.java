package com.conduit.backend.articles.domain;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository {

	UUID nextId();

	void add(Article article);

	boolean update(Article article);

	Optional<Article> findBySlug(String slug);

	boolean existsBySlug(String slug, UUID excludeId);

	boolean deleteById(UUID id);
}
