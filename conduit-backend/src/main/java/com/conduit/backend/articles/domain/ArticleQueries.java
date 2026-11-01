package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.PageWindow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleQueries {

	Optional<ArticleView> findBySlug(String slug, UUID viewerId);

	ArticleViewPage search(ArticleFilter filter, UUID viewerId);

	ArticleViewPage feed(UUID viewerId, PageWindow window);

	List<String> allTags();
}
