package com.conduit.backend.comments.infrastructure;

import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentQueries;
import com.conduit.backend.comments.domain.CommentView;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.infrastructure.persistence.AuthorProfiles;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCommentQueries implements CommentQueries {

	private final CommentJpaRepository jpaRepository;

	private final AuthorProfiles authorProfiles;

	public JpaCommentQueries(CommentJpaRepository jpaRepository, AuthorProfiles authorProfiles) {
		this.jpaRepository = jpaRepository;
		this.authorProfiles = authorProfiles;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommentView> findByArticle(UUID articleId, UUID viewerId) {
		List<CommentEntity> entities = jpaRepository.findByArticle(articleId);
		Set<UUID> authorIds = new HashSet<>();
		entities.forEach(entity -> authorIds.add(entity.getAuthorId()));
		Map<UUID, Profile> authors = authorProfiles.load(authorIds, viewerId);
		return entities.stream()
				.map(entity -> new CommentView(entity.toDomain(), authors.get(entity.getAuthorId())))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CommentView viewOf(Comment comment, UUID viewerId) {
		Map<UUID, Profile> authors = authorProfiles.load(Set.of(comment.getAuthorId()), viewerId);
		return new CommentView(comment, authors.get(comment.getAuthorId()));
	}
}
