package com.conduit.backend.articles.infrastructure;

import com.conduit.backend.articles.domain.ArticleFilter;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.articles.domain.ArticleView;
import com.conduit.backend.articles.domain.ArticleViewPage;
import com.conduit.backend.shared.domain.PageWindow;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.infrastructure.persistence.AuthorProfiles;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaArticleQueries implements ArticleQueries {

	private static final int TAG_LIMIT = 20;

	private static final String ORDER = " order by a.created_at desc, a.id desc";

	private final EntityManager entityManager;

	private final AuthorProfiles authorProfiles;

	public JpaArticleQueries(EntityManager entityManager, AuthorProfiles authorProfiles) {
		this.entityManager = entityManager;
		this.authorProfiles = authorProfiles;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ArticleView> findBySlug(String slug, UUID viewerId) {
		List<?> rows = entityManager.createNativeQuery("select a.id from articles a where a.slug = :slug")
				.setParameter("slug", slug)
				.getResultList();
		if (rows.isEmpty()) {
			return Optional.empty();
		}
		List<ArticleView> views = hydrate(List.of((UUID) rows.get(0)), viewerId);
		return views.stream().findFirst();
	}

	@Override
	@Transactional(readOnly = true)
	public ArticleViewPage search(ArticleFilter filter, UUID viewerId) {
		StringBuilder where = new StringBuilder(" from articles a where 1 = 1");
		Map<String, Object> parameters = new LinkedHashMap<>();
		if (filter.tag() != null) {
			where.append(" and exists (select 1 from article_tags t where t.article_id = a.id and t.tag = :tag)");
			parameters.put("tag", filter.tag());
		}
		if (filter.author() != null) {
			where.append(" and a.author_id in (select u.id from users u where lower(u.username) = lower(:author))");
			parameters.put("author", filter.author());
		}
		if (filter.favoritedBy() != null) {
			where.append(" and exists (select 1 from favorites f join users fu on fu.id = f.user_id"
					+ " where f.article_id = a.id and lower(fu.username) = lower(:favoritedBy))");
			parameters.put("favoritedBy", filter.favoritedBy());
		}
		return page(where.toString(), parameters, filter.window(), viewerId);
	}

	@Override
	@Transactional(readOnly = true)
	public ArticleViewPage feed(UUID viewerId, PageWindow window) {
		String where = " from articles a where a.author_id in"
				+ " (select fl.followee_id from follows fl where fl.follower_id = :viewer)";
		return page(where, Map.of("viewer", viewerId), window, viewerId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> allTags() {
		List<?> rows = entityManager
				.createNativeQuery("select tag from article_tags group by tag order by count(*) desc, tag asc")
				.setMaxResults(TAG_LIMIT)
				.getResultList();
		return rows.stream().map(String.class::cast).toList();
	}

	private ArticleViewPage page(String where, Map<String, Object> parameters, PageWindow window, UUID viewerId) {
		Query countQuery = entityManager.createNativeQuery("select count(*)" + where);
		parameters.forEach(countQuery::setParameter);
		long total = ((Number) countQuery.getSingleResult()).longValue();

		Query idQuery = entityManager.createNativeQuery("select a.id" + where + ORDER);
		parameters.forEach(idQuery::setParameter);
		idQuery.setFirstResult(window.offset());
		idQuery.setMaxResults(window.limit());
		List<UUID> ids = new ArrayList<>();
		for (Object row : idQuery.getResultList()) {
			ids.add((UUID) row);
		}
		return new ArticleViewPage(hydrate(ids, viewerId), total);
	}

	private List<ArticleView> hydrate(List<UUID> ids, UUID viewerId) {
		if (ids.isEmpty()) {
			return List.of();
		}
		Map<UUID, ArticleEntity> entities = new HashMap<>();
		entityManager.createQuery("select a from ArticleEntity a where a.id in :ids", ArticleEntity.class)
				.setParameter("ids", ids)
				.getResultList()
				.forEach(entity -> entities.put(entity.getId(), entity));

		Map<UUID, List<String>> tags = tagsByArticle(ids);
		Map<UUID, Long> counts = favoriteCounts(ids);
		Set<UUID> favorited = viewerId == null ? Set.of() : favoritedBy(ids, viewerId);

		Set<UUID> authorIds = new HashSet<>();
		entities.values().forEach(entity -> authorIds.add(entity.getAuthorId()));
		Map<UUID, Profile> authors = authorProfiles.load(authorIds, viewerId);

		List<ArticleView> views = new ArrayList<>();
		for (UUID id : ids) {
			ArticleEntity entity = entities.get(id);
			if (entity == null) {
				continue;
			}
			views.add(new ArticleView(
					entity.toDomain(tags.getOrDefault(id, List.of())),
					authors.get(entity.getAuthorId()),
					favorited.contains(id),
					counts.getOrDefault(id, 0L)));
		}
		return views;
	}

	private Map<UUID, List<String>> tagsByArticle(List<UUID> ids) {
		List<?> rows = entityManager
				.createNativeQuery("select article_id, tag from article_tags where article_id in (:ids) order by position")
				.setParameter("ids", ids)
				.getResultList();
		Map<UUID, List<String>> tags = new HashMap<>();
		for (Object row : rows) {
			Object[] columns = (Object[]) row;
			tags.computeIfAbsent((UUID) columns[0], key -> new ArrayList<>()).add((String) columns[1]);
		}
		return tags;
	}

	private Map<UUID, Long> favoriteCounts(List<UUID> ids) {
		List<?> rows = entityManager
				.createNativeQuery(
						"select article_id, count(*) from favorites where article_id in (:ids) group by article_id")
				.setParameter("ids", ids)
				.getResultList();
		Map<UUID, Long> counts = new HashMap<>();
		for (Object row : rows) {
			Object[] columns = (Object[]) row;
			counts.put((UUID) columns[0], ((Number) columns[1]).longValue());
		}
		return counts;
	}

	private Set<UUID> favoritedBy(List<UUID> ids, UUID viewerId) {
		List<?> rows = entityManager
				.createNativeQuery("select article_id from favorites where user_id = :viewer and article_id in (:ids)")
				.setParameter("viewer", viewerId)
				.setParameter("ids", ids)
				.getResultList();
		Set<UUID> favorited = new HashSet<>();
		for (Object row : rows) {
			favorited.add((UUID) row);
		}
		return favorited;
	}
}
