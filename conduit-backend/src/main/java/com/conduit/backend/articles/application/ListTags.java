package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.ArticleQueries;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListTags {

	private final ArticleQueries articleQueries;

	public ListTags(ArticleQueries articleQueries) {
		this.articleQueries = articleQueries;
	}

	public List<String> execute() {
		return articleQueries.allTags();
	}
}
