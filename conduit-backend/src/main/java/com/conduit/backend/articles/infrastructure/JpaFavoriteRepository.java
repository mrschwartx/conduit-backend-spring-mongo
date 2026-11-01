package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.FavoriteRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaFavoriteRepository implements FavoriteRepository {

	private final EntityManager entityManager;

	public JpaFavoriteRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public void favorite(UUID userId, UUID articleId) {
		entityManager.createNativeQuery(
				"insert into favorites (user_id, article_id) values (:user, :article) on conflict do nothing")
				.setParameter("user", userId)
				.setParameter("article", articleId)
				.executeUpdate();
	}

	@Override
	@Transactional
	public void unfavorite(UUID userId, UUID articleId) {
		entityManager.createNativeQuery("delete from favorites where user_id = :user and article_id = :article")
				.setParameter("user", userId)
				.setParameter("article", articleId)
				.executeUpdate();
	}
}
