package com.conduit.backend.comments.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

	@Query("select c from CommentEntity c where c.articleId = :articleId order by c.createdAt desc, c.id desc")
	List<CommentEntity> findByArticle(@Param("articleId") UUID articleId);

	@Modifying
	@Query("delete from CommentEntity c where c.id = :id")
	int deleteCommentById(@Param("id") long id);
}
