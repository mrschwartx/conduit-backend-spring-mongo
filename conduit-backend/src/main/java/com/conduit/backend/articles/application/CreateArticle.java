package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.articles.domain.Body;
import com.conduit.backend.articles.domain.Description;
import com.conduit.backend.articles.domain.Tags;
import com.conduit.backend.articles.domain.Title;
import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Validation;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateArticle {

	private static final int MAX_ATTEMPTS = 3;

	private final ArticleRepository articleRepository;

	private final ArticleQueries articleQueries;

	private final UniqueSlugs uniqueSlugs;

	public CreateArticle(ArticleRepository articleRepository, ArticleQueries articleQueries,
			UniqueSlugs uniqueSlugs) {
		this.articleRepository = articleRepository;
		this.articleQueries = articleQueries;
		this.uniqueSlugs = uniqueSlugs;
	}

	public ArticleDto execute(UUID authorId, Map<String, Object> input) {
		Validation validation = new Validation();
		Title title = validation.check(() -> Title.create(input.get("title")));
		Description description = validation.check(() -> Description.create(input.get("description")));
		Body body = validation.check(() -> Body.create(input.get("body")));
		Tags tags = validation.check(() -> Tags.create(input.get("tagList")));
		validation.failIfAny();

		Article article = save(authorId, title, description, body, tags);

		return articleQueries.findBySlug(article.getSlug().value(), authorId)
				.map(ArticleDto::from)
				.orElseThrow(() -> new NotFoundException("article"));
	}

	private Article save(UUID authorId, Title title, Description description, Body body, Tags tags) {
		for (int attempt = 1;; attempt++) {
			Article article = Article.create(articleRepository.nextId(), authorId, uniqueSlugs.next(title, null),
					title, description, body, tags);
			try {
				articleRepository.add(article);
				return article;
			}
			catch (AlreadyExistsException error) {
				if (attempt >= MAX_ATTEMPTS) {
					throw error;
				}
			}
		}
	}
}
