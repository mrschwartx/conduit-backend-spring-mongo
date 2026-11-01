package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleView;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.domain.Timestamps;
import java.util.List;

public record ArticleSummaryDto(
		String slug,
		String title,
		String description,
		List<String> tagList,
		String createdAt,
		String updatedAt,
		boolean favorited,
		long favoritesCount,
		Profile author) {

	public static ArticleSummaryDto from(ArticleView view) {
		Article article = view.article();
		return new ArticleSummaryDto(
				article.getSlug().value(),
				article.getTitle().value(),
				article.getDescription().value(),
				article.getTags().values(),
				Timestamps.format(article.getCreatedAt()),
				Timestamps.format(article.getUpdatedAt()),
				view.favorited(),
				view.favoritesCount(),
				view.author());
	}
}
