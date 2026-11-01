package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.ArticleFilter;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.shared.domain.PageWindow;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListArticles {

	private final ArticleQueries articleQueries;

	public ListArticles(ArticleQueries articleQueries) {
		this.articleQueries = articleQueries;
	}

	public ArticleListDto execute(UUID viewerId, String tag, String author, String favorited, String offset,
			String limit) {
		PageWindow window = PageWindow.parse(offset, limit);
		ArticleFilter filter = new ArticleFilter(blankToNull(tag), blankToNull(author), blankToNull(favorited),
				window);
		return ArticleListDto.from(articleQueries.search(filter, viewerId));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
