package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.articles.domain.Slug;
import com.conduit.backend.articles.domain.Title;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UniqueSlugs {

	private final ArticleRepository articleRepository;

	public UniqueSlugs(ArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	public Slug next(Title title, UUID excludeId) {
		Slug base = Slug.base(title);
		Slug candidate = base;
		int number = 2;
		while (candidate.isReserved() || articleRepository.existsBySlug(candidate.value(), excludeId)) {
			candidate = base.withSuffix(number++);
		}
		return candidate;
	}
}
