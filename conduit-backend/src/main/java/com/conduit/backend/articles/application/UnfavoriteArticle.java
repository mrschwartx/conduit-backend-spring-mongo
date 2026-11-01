package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.articles.domain.FavoriteRepository;
import com.conduit.backend.shared.domain.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UnfavoriteArticle {

	private final ArticleRepository articleRepository;

	private final FavoriteRepository favoriteRepository;

	private final ArticleQueries articleQueries;

	public UnfavoriteArticle(ArticleRepository articleRepository, FavoriteRepository favoriteRepository,
			ArticleQueries articleQueries) {
		this.articleRepository = articleRepository;
		this.favoriteRepository = favoriteRepository;
		this.articleQueries = articleQueries;
	}

	public ArticleDto execute(UUID userId, String slug) {
		Article article = articleRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
		favoriteRepository.unfavorite(userId, article.getId());
		return articleQueries.findBySlug(slug, userId)
				.map(ArticleDto::from)
				.orElseThrow(() -> new NotFoundException("article"));
	}
}
