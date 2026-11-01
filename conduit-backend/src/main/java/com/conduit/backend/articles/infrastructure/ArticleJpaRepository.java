package com.conduit.backend.articles.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ArticleJpaRepository extends JpaRepository<ArticleEntity, UUID> {

	Optional<ArticleEntity> findBySlug(String slug);

	long countBySlugAndIdNot(String slug, UUID id);

	@Modifying
	@Query("delete from ArticleEntity a where a.id = :id")
	int deleteArticleById(@Param("id") UUID id);
}
