package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.ArticleViewPage;
import java.util.List;

public record ArticleListDto(List<ArticleSummaryDto> articles, long articlesCount) {

	public static ArticleListDto from(ArticleViewPage page) {
		return new ArticleListDto(page.articles().stream().map(ArticleSummaryDto::from).toList(), page.total());
	}
}
