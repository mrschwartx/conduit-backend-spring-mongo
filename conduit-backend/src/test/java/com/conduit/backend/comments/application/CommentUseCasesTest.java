package com.conduit.backend.comments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.comments.domain.ArticleLookup;
import com.conduit.backend.comments.domain.Comment;
import com.conduit.backend.comments.domain.CommentQueries;
import com.conduit.backend.comments.domain.CommentRepository;
import com.conduit.backend.comments.domain.CommentView;
import com.conduit.backend.shared.domain.ForbiddenException;
import com.conduit.backend.shared.domain.NotFoundException;
import com.conduit.backend.shared.domain.Profile;
import com.conduit.backend.shared.domain.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommentUseCasesTest {

	private final UUID articleId = UUID.randomUUID();

	private final UUID otherArticleId = UUID.randomUUID();

	private final UUID author = UUID.randomUUID();

	private final UUID stranger = UUID.randomUUID();

	private final List<Comment> stored = new ArrayList<>();

	private AddComment addComment;

	private ListComments listComments;

	private DeleteComment deleteComment;

	private static CommentView view(Comment comment) {
		return new CommentView(comment, new Profile("author", null, null, false));
	}

	@BeforeEach
	void setUp() {
		ArticleLookup lookup = slug -> switch (slug) {
			case "first" -> Optional.of(articleId);
			case "second" -> Optional.of(otherArticleId);
			default -> Optional.empty();
		};
		CommentRepository repository = new CommentRepository() {

			@Override
			public Comment add(Comment comment) {
				Comment saved = Comment.restore((long) stored.size() + 1, comment.getArticleId(),
						comment.getAuthorId(), comment.getBody(), comment.getCreatedAt(), comment.getUpdatedAt());
				stored.add(saved);
				return saved;
			}

			@Override
			public Optional<Comment> findById(long id) {
				return stored.stream().filter(comment -> comment.getId() == id).findFirst();
			}

			@Override
			public boolean deleteById(long id) {
				return stored.removeIf(comment -> comment.getId() == id);
			}
		};
		CommentQueries queries = new CommentQueries() {

			@Override
			public List<CommentView> findByArticle(UUID id, UUID viewerId) {
				return stored.stream().filter(comment -> comment.belongsTo(id)).map(CommentUseCasesTest::view)
						.toList();
			}

			@Override
			public CommentView viewOf(Comment comment, UUID viewerId) {
				return view(comment);
			}
		};
		addComment = new AddComment(lookup, repository, queries);
		listComments = new ListComments(lookup, queries);
		deleteComment = new DeleteComment(lookup, repository);
	}

	@Test
	void addsAndListsComments() {
		CommentDto created = addComment.execute(author, "first", Map.of("body", "Nice"));
		addComment.execute(author, "second", Map.of("body", "Elsewhere"));

		assertThat(created.id()).isEqualTo(1);
		assertThat(created.body()).isEqualTo("Nice");
		assertThat(listComments.execute(null, "first")).extracting(CommentDto::body).containsExactly("Nice");
	}

	@Test
	void addRejectsBlankBodiesAndUnknownArticles() {
		assertThatThrownBy(() -> addComment.execute(author, "first", Map.of("body", "  ")))
				.isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> addComment.execute(author, "missing", Map.of("body", "x")))
				.isInstanceOf(NotFoundException.class);
		assertThatThrownBy(() -> listComments.execute(null, "missing")).isInstanceOf(NotFoundException.class);
	}

	@Test
	void deleteIsRestrictedToTheCommentAuthor() {
		addComment.execute(author, "first", Map.of("body", "Nice"));

		assertThatThrownBy(() -> deleteComment.execute(stranger, "first", "1"))
				.isInstanceOf(ForbiddenException.class)
				.extracting("errors").isEqualTo(Map.of("comment", List.of("forbidden")));
		deleteComment.execute(author, "first", "1");
		assertThat(stored).isEmpty();
	}

	@Test
	void deleteRequiresAMatchingArticleAndValidId() {
		addComment.execute(author, "first", Map.of("body", "Nice"));

		assertThatThrownBy(() -> deleteComment.execute(author, "second", "1")).isInstanceOf(NotFoundException.class);
		assertThatThrownBy(() -> deleteComment.execute(author, "first", "abc")).isInstanceOf(NotFoundException.class);
		assertThatThrownBy(() -> deleteComment.execute(author, "first", "99")).isInstanceOf(NotFoundException.class);
		assertThatThrownBy(() -> deleteComment.execute(author, "missing", "1")).isInstanceOf(NotFoundException.class);
	}
}
