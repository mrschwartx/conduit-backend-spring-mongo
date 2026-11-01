package com.conduit.backend.comments.presentation;

import com.conduit.backend.comments.application.AddComment;
import com.conduit.backend.comments.application.CommentDto;
import com.conduit.backend.comments.application.DeleteComment;
import com.conduit.backend.comments.application.ListComments;
import com.conduit.backend.shared.application.AuthenticatedUser;
import com.conduit.backend.shared.infrastructure.http.RequestBodies;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles/{slug}/comments")
public class CommentsController {

	private final AddComment addComment;

	private final ListComments listComments;

	private final DeleteComment deleteComment;

	public CommentsController(AddComment addComment, ListComments listComments, DeleteComment deleteComment) {
		this.addComment = addComment;
		this.listComments = listComments;
		this.deleteComment = deleteComment;
	}

	@GetMapping
	public Map<String, List<CommentDto>> list(@AuthenticationPrincipal AuthenticatedUser viewer,
			@PathVariable String slug) {
		return Map.of("comments", listComments.execute(viewer != null ? viewer.userId() : null, slug));
	}

	@PostMapping
	public ResponseEntity<Map<String, CommentDto>> create(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String slug, @RequestBody(required = false) Map<String, Object> body) {
		UUID authorId = requester.userId();
		CommentDto comment = addComment.execute(authorId, slug, RequestBodies.section(body, "comment"));
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("comment", comment));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser requester,
			@PathVariable String slug, @PathVariable String id) {
		deleteComment.execute(requester.userId(), slug, id);
		return ResponseEntity.noContent().build();
	}
}
