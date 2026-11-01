package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.ArticleLookup;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleLookup implements ArticleLookup {

	private final EntityManager entityManager;

	public JpaArticleLookup(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Optional<UUID> findIdBySlug(String slug) {
		List<?> rows = entityManager.createNativeQuery("select id from articles where slug = :slug")
				.setParameter("slug", slug)
				.getResultList();
		return rows.stream().findFirst().map(UUID.class::cast);
	}
}
