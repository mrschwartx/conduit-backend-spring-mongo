package com.conduit.backend.articles.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.shared.domain.ValidationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ArticleDomainTest {

	private static final Instant NOW = Instant.parse("2026-10-25T00:00:00Z");

	private static Article article() {
		return Article.create(UUID.randomUUID(), UUID.randomUUID(), new Slug("how-to-train-your-dragon"),
				new Title("How to Train Your Dragon"),
				new Description("Ever wonder how?"), new Body("It takes a Jacobian"),
				Tags.create(List.of("dragons", "training")), NOW);
	}

	@Test
	void slugIsDerivedFromTheTitle() {
		assertThat(Slug.base(new Title("How to Train Your Dragon!")).value()).isEqualTo("how-to-train-your-dragon");
		assertThat(Slug.base(new Title("  Café --- Crème  ")).value()).isEqualTo("cafe-creme");
		assertThat(Slug.base(new Title("日本語")).value()).isEqualTo("article");
	}

	@Test
	void tagsAreTrimmedDeduplicatedAndKeepFirstSeenOrder() {
		assertThat(Tags.create(List.of(" b ", "a", "b")).values()).containsExactly("b", "a");
		assertThat(Tags.create(null).values()).isEmpty();
	}

	@Test
	void tagsRejectInvalidInput() {
		assertThatThrownBy(() -> Tags.create("a")).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Tags.create(List.of(1))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Tags.create(List.of(" "))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Tags.create(List.of("x".repeat(31)))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Tags.create(List.of("a b"))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Tags.create(List.of("a,b"))).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> Tags.create(java.util.stream.IntStream.range(0, 11).mapToObj(i -> "t" + i).toList()))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void requiredFieldsMustNotBeBlank() {
		assertThatThrownBy(() -> Title.create(" "))
				.extracting("errors").isEqualTo(Map.of("title", List.of("can't be blank")));
		assertThatThrownBy(() -> Description.create(null))
				.extracting("errors").isEqualTo(Map.of("description", List.of("can't be blank")));
		assertThatThrownBy(() -> Body.create(""))
				.extracting("errors").isEqualTo(Map.of("body", List.of("can't be blank")));
	}

	@Test
	void createsWithSlugAndTimestamps() {
		Article article = article();

		assertThat(article.getSlug().value()).isEqualTo("how-to-train-your-dragon");
		assertThat(article.getCreatedAt()).isEqualTo(NOW);
		assertThat(article.getUpdatedAt()).isEqualTo(NOW);
	}

	@Test
	void slugsAreCappedAndSuffixed() {
		assertThat(Slug.base(new Title("a".repeat(100))).value()).hasSize(80);
		assertThat(Slug.base(new Title("Feed")).isReserved()).isTrue();
		assertThat(Slug.base(new Title("Feed")).withSuffix(2).value()).isEqualTo("feed-2");
	}

	@Test
	void changingTheTitleAppliesTheNewSlug() {
		Article article = article();
		Instant later = NOW.plusSeconds(30);

		article.update(new ArticleChanges(new Title("Did You Train Your Dragon"), null, null, null),
				new Slug("did-you-train-your-dragon"), later);

		assertThat(article.getSlug().value()).isEqualTo("did-you-train-your-dragon");
		assertThat(article.getBody().value()).isEqualTo("It takes a Jacobian");
		assertThat(article.getUpdatedAt()).isEqualTo(later);
	}

	@Test
	void keepsTheSlugWhenTheTitleIsUntouched() {
		Article article = article();

		article.update(new ArticleChanges(null, null, new Body("new body"), Tags.empty()), null, NOW.plusSeconds(1));

		assertThat(article.getSlug().value()).isEqualTo("how-to-train-your-dragon");
		assertThat(article.getTags().values()).isEmpty();
	}

	@Test
	void knowsItsAuthor() {
		Article article = article();

		assertThat(article.isWrittenBy(article.getAuthorId())).isTrue();
		assertThat(article.isWrittenBy(UUID.randomUUID())).isFalse();
	}
}
