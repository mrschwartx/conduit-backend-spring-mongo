package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.shared.domain.ForbiddenException;
import com.conduit.backend.shared.domain.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeleteArticle {

	private final ArticleRepository articleRepository;

	public DeleteArticle(ArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	public void execute(UUID requesterId, String slug) {
		Article article = articleRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
		if (!article.isWrittenBy(requesterId)) {
			throw new ForbiddenException("article");
		}
		if (!articleRepository.deleteById(article.getId())) {
			throw new NotFoundException("article");
		}
	}
}
