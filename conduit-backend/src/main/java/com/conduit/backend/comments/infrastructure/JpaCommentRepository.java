package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCommentRepository implements CommentRepository {

	private final CommentJpaRepository jpaRepository;

	private final EntityManager entityManager;

	public JpaCommentRepository(CommentJpaRepository jpaRepository, EntityManager entityManager) {
		this.jpaRepository = jpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public Comment add(Comment comment) {
		CommentEntity entity = CommentEntity.from(comment);
		entityManager.persist(entity);
		entityManager.flush();
		return entity.toDomain();
	}

	@Override
	public Optional<Comment> findById(long id) {
		return jpaRepository.findById(id).map(CommentEntity::toDomain);
	}

	@Override
	@Transactional
	public boolean deleteById(long id) {
		return jpaRepository.deleteCommentById(id) == 1;
	}
}
