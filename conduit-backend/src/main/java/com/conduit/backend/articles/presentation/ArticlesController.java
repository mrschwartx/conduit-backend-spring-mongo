package com.conduit.backend.articles.presentation;

import com.conduit.backend.articles.application.ArticleDto;
import com.conduit.backend.articles.application.ArticleListDto;
import com.conduit.backend.articles.application.CreateArticle;
import com.conduit.backend.articles.application.DeleteArticle;
import com.conduit.backend.articles.application.FavoriteArticle;
import com.conduit.backend.articles.application.GetArticle;
import com.conduit.backend.articles.application.GetFeed;
import com.conduit.backend.articles.application.ListArticles;
import com.conduit.backend.articles.application.UnfavoriteArticle;
import com.conduit.backend.articles.application.UpdateArticle;
import com.conduit.backend.shared.application.AuthenticatedUser;
import com.conduit.backend.shared.infrastructure.http.RequestBodies;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticlesController {

	private final CreateArticle createArticle;

	private final GetArticle getArticle;

	private final ListArticles listArticles;

	private final GetFeed getFeed;

	private final UpdateArticle updateArticle;

	private final DeleteArticle deleteArticle;

	private final FavoriteArticle favoriteArticle;

	private final UnfavoriteArticle unfavoriteArticle;

	public ArticlesController(CreateArticle createArticle, GetArticle getArticle, ListArticles listArticles,
			GetFeed getFeed, UpdateArticle updateArticle, DeleteArticle deleteArticle,
			FavoriteArticle favoriteArticle, UnfavoriteArticle unfavoriteArticle) {
		this.createArticle = createArticle;
		this.getArticle = getArticle;
		this.listArticles = listArticles;
		this.getFeed = getFeed;
		this.updateArticle = updateArticle;
		this.deleteArticle = deleteArticle;
		this.favoriteArticle = favoriteArticle;
		this.unfavoriteArticle = unfavoriteArticle;
	}

	@GetMapping("/feed")
	public ArticleListDto feed(@AuthenticationPrincipal AuthenticatedUser requester,
			@RequestParam(name = "offset", required = false) String offset,
			@RequestParam(name = "limit", required = false) String limit) {
		return getFeed.execute(requester.userId(), offset, limit);
	}

	@GetMapping
	public ArticleListDto list(@AuthenticationPrincipal AuthenticatedUser viewer,
			@RequestParam(name = "tag", required = false) String tag,
			@RequestParam(name = "author", required = false) String author,
			@RequestParam(name = "favorited", required = false) String favorited,
			@RequestParam(name = "offset", required = false) String offset,
			@RequestParam(name = "limit", required = false) String limit) {
		return listArticles.execute(viewerId(viewer), tag, author, favorited, offset, limit);
	}

	@PostMapping
	public ResponseEntity<Map<String, ArticleDto>> create(@AuthenticationPrincipal AuthenticatedUser requester,
			@RequestBody(required = false) Map<String, Object> body) {
		ArticleDto article = createArticle.execute(requester.userId(), RequestBodies.section(body, "article"));
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("article", article));
	}

	@GetMapping("/{slug}")
	public Map<String, ArticleDto> get(@AuthenticationPrincipal AuthenticatedUser viewer,
			@PathVariable String slug) {
		return Map.of("article", getArticle.execute(viewerId(viewer), slug));
	}

	@PutMapping("/{slug}")
	public Map<String, ArticleDto> update(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String slug, @RequestBody(required = false) Map<String, Object> body) {
		return Map.of("article",
				updateArticle.execute(requester.userId(), slug, RequestBodies.section(body, "article")));
	}

	@DeleteMapping("/{slug}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String slug) {
		deleteArticle.execute(requester.userId(), slug);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{slug}/favorite")
	public Map<String, ArticleDto> favorite(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String slug) {
		return Map.of("article", favoriteArticle.execute(requester.userId(), slug));
	}

	@DeleteMapping("/{slug}/favorite")
	public Map<String, ArticleDto> unfavorite(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String slug) {
		return Map.of("article", unfavoriteArticle.execute(requester.userId(), slug));
	}

	private static UUID viewerId(AuthenticatedUser viewer) {
		return viewer != null ? viewer.userId() : null;
	}
}
