package com.conduit.backend.articles.application;

import com.conduit.backend.articles.domain.Article;
import com.conduit.backend.articles.domain.ArticleChanges;
import com.conduit.backend.articles.domain.ArticleQueries;
import com.conduit.backend.articles.domain.ArticleRepository;
import com.conduit.backend.articles.domain.Body;
import com.conduit.backend.articles.domain.Description;
import com.conduit.backend.articles.domain.Slug;
import com.conduit.backend.articles.domain.Tags;
import com.conduit.backend.articles.domain.Title;
import com.conduit.backend.shared.domain.ForbiddenException;
import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Validation;
import com.conduit.backend.shared.domain.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateArticle {

	private final ArticleRepository articleRepository;

	private static final List<String> FIELDS = List.of("title", "description", "body", "tagList");

	private final ArticleQueries articleQueries;

	private final UniqueSlugs uniqueSlugs;

	public UpdateArticle(ArticleRepository articleRepository, ArticleQueries articleQueries,
			UniqueSlugs uniqueSlugs) {
		this.articleRepository = articleRepository;
		this.articleQueries = articleQueries;
		this.uniqueSlugs = uniqueSlugs;
	}

	public ArticleDto execute(UUID requesterId, String slug, Map<String, Object> input) {
		Article article = articleRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
		if (!article.isWrittenBy(requesterId)) {
			throw new ForbiddenException("article");
		}

		ArticleChanges changes = parseChanges(input);
		Slug slugChange = changes.title() != null && !changes.title().equals(article.getTitle())
				? uniqueSlugs.next(changes.title(), article.getId()) : null;
		article.update(changes, slugChange);

		if (!articleRepository.update(article)) {
			throw new NotFoundException("article");
		}

		return articleQueries.findBySlug(article.getSlug().value(), requesterId)
				.map(ArticleDto::from)
				.orElseThrow(() -> new NotFoundException("article"));
	}

	private static ArticleChanges parseChanges(Map<String, Object> source) {
		if (!FIELDS.stream().anyMatch(source::containsKey)) {
			throw new ValidationException("article", "can't be empty");
		}
		Validation validation = new Validation();
		Title title = source.containsKey("title") ? validation.check(() -> Title.create(source.get("title"))) : null;
		Description description = source.containsKey("description")
				? validation.check(() -> Description.create(source.get("description"))) : null;
		Body body = source.containsKey("body") ? validation.check(() -> Body.create(source.get("body"))) : null;
		Tags tags = source.containsKey("tagList") ? validation.check(() -> Tags.create(source.get("tagList"))) : null;
		validation.failIfAny();
		return new ArticleChanges(title, description, body, tags);
	}
}
