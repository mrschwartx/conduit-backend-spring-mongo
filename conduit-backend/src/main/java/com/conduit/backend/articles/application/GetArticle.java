package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.shared.domain.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetArticle {

	private final ArticleQueries articleQueries;

	public GetArticle(ArticleQueries articleQueries) {
		this.articleQueries = articleQueries;
	}

	public ArticleDto execute(UUID viewerId, String slug) {
		return articleQueries.findBySlug(slug, viewerId)
				.map(ArticleDto::from)
				.orElseThrow(() -> new NotFoundException("article"));
	}
}
