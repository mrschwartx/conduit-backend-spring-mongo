package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.shared.infrastructure.persistence.UniqueViolations;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaArticleRepository implements ArticleRepository {

	private static final UUID NO_EXCLUSION = new UUID(0L, 0L);

	private final ArticleJpaRepository jpaRepository;

	private final EntityManager entityManager;

	public JpaArticleRepository(ArticleJpaRepository jpaRepository, EntityManager entityManager) {
		this.jpaRepository = jpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	@Transactional
	public void add(Article article) {
		try {
			entityManager.persist(ArticleEntity.from(article));
			entityManager.flush();
			insertTags(article.getId(), article.getTags().values());
		}
		catch (RuntimeException error) {
			throw translate(error);
		}
	}

	@Override
	@Transactional
	public boolean update(Article article) {
		ArticleEntity entity = entityManager.find(ArticleEntity.class, article.getId());
		if (entity == null) {
			return false;
		}
		try {
			entity.apply(article);
			entityManager.flush();
			entityManager.createNativeQuery("delete from article_tags where article_id = :id")
					.setParameter("id", article.getId())
					.executeUpdate();
			insertTags(article.getId(), article.getTags().values());
			return true;
		}
		catch (RuntimeException error) {
			throw translate(error);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Article> findBySlug(String slug) {
		return jpaRepository.findBySlug(slug).map(entity -> entity.toDomain(tagsOf(entity.getId())));
	}

	@Override
	public boolean existsBySlug(String slug, UUID excludeId) {
		return jpaRepository.countBySlugAndIdNot(slug, excludeId != null ? excludeId : NO_EXCLUSION) > 0;
	}

	@Override
	@Transactional
	public boolean deleteById(UUID id) {
		return jpaRepository.deleteArticleById(id) == 1;
	}

	private void insertTags(UUID articleId, List<String> tags) {
		for (int position = 0; position < tags.size(); position++) {
			entityManager.createNativeQuery(
					"insert into article_tags (article_id, tag, position) values (:id, :tag, :position)")
					.setParameter("id", articleId)
					.setParameter("tag", tags.get(position))
					.setParameter("position", position)
					.executeUpdate();
		}
	}

	private List<String> tagsOf(UUID articleId) {
		List<?> rows = entityManager
				.createNativeQuery("select tag from article_tags where article_id = :id order by position")
				.setParameter("id", articleId)
				.getResultList();
		return rows.stream().map(String.class::cast).toList();
	}

	private static RuntimeException translate(RuntimeException error) {
		if (UniqueViolations.isUniqueViolation(error)) {
			return new AlreadyExistsException("title");
		}
		return error;
	}
}
