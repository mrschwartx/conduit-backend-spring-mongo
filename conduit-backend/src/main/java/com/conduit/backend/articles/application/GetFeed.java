package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.shared.domain.PageWindow;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetFeed {

	private final ArticleQueries articleQueries;

	public GetFeed(ArticleQueries articleQueries) {
		this.articleQueries = articleQueries;
	}

	public ArticleListDto execute(UUID viewerId, String offset, String limit) {
		return ArticleListDto.from(articleQueries.feed(viewerId, PageWindow.parse(offset, limit)));
	}
}
