package com.conduit.backend.support;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleFilter;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.articles.domain.ArticleView;
import com.conduit.backend.articles.domain.ArticleViewPage;
import com.conduit.backend.shared.domain.EntityIds;
import com.conduit.backend.shared.domain.PageWindow;
import com.conduit.backend.shared.domain.Profile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryArticleStore implements ArticleRepository, ArticleQueries {

	private final Map<UUID, Article> articles = new LinkedHashMap<>();

	@Override
	public UUID nextId() {
		return EntityIds.generate();
	}

	@Override
	public void add(Article article) {
		articles.put(article.getId(), article);
	}

	@Override
	public boolean update(Article article) {
		if (!articles.containsKey(article.getId())) {
			return false;
		}
		articles.put(article.getId(), article);
		return true;
	}

	@Override
	public Optional<Article> findBySlug(String slug) {
		return articles.values().stream().filter(article -> article.getSlug().value().equals(slug)).findFirst();
	}

	@Override
	public boolean existsBySlug(String slug, UUID excludeId) {
		return articles.values().stream()
				.filter(article -> !article.getId().equals(excludeId))
				.anyMatch(article -> article.getSlug().value().equals(slug));
	}

	@Override
	public boolean deleteById(UUID id) {
		return articles.remove(id) != null;
	}

	@Override
	public Optional<ArticleView> findBySlug(String slug, UUID viewerId) {
		return findBySlug(slug).map(article -> new ArticleView(article,
				new Profile("author", null, null, false), false, 0));
	}

	@Override
	public ArticleViewPage search(ArticleFilter filter, UUID viewerId) {
		return new ArticleViewPage(List.of(), 0);
	}

	@Override
	public ArticleViewPage feed(UUID viewerId, PageWindow window) {
		return new ArticleViewPage(List.of(), 0);
	}

	@Override
	public List<String> allTags() {
		return articles.values().stream().flatMap(article -> article.getTags().values().stream()).distinct()
				.sorted().toList();
	}
}
