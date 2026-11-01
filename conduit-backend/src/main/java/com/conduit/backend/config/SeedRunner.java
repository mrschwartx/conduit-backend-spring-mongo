package com.conduit.backend.config;

import com.conduit.backend.articles.application.ArticleDto;
import com.conduit.backend.articles.application.CreateArticle;
import com.conduit.backend.comments.application.AddComment;
import com.conduit.backend.shared.domain.AlreadyExistsException;
import com.conduit.backend.users.application.FollowUser;
import com.conduit.backend.users.application.RegisterUser;
import com.conduit.backend.users.application.UserDto;
import com.conduit.backend.users.application.VerifyAccessToken;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "conduit.seed.enabled", havingValue = "true")
public class SeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

	private final ConduitProperties properties;

	private final RegisterUser registerUser;

	private final CreateArticle createArticle;

	private final AddComment addComment;

	private final FollowUser followUser;

	private final VerifyAccessToken verifyAccessToken;

	private final ApplicationContext context;

	public SeedRunner(ConduitProperties properties, RegisterUser registerUser, CreateArticle createArticle,
			AddComment addComment, FollowUser followUser, VerifyAccessToken verifyAccessToken,
			ApplicationContext context) {
		this.properties = properties;
		this.registerUser = registerUser;
		this.createArticle = createArticle;
		this.addComment = addComment;
		this.followUser = followUser;
		this.verifyAccessToken = verifyAccessToken;
		this.context = context;
	}

	@Override
	public void run(ApplicationArguments args) {
		int exitCode = seed();
		System.exit(SpringApplication.exit(context, () -> exitCode));
	}

	private int seed() {
		if (properties.app().production() && !properties.seed().allowProduction()) {
			log.error("Refusing to seed a production database");
			return 1;
		}
		try {
			UUID john = registerOrSkip("johndoe123", "johndoe123@mail.com", "password123");
			UUID jane = registerOrSkip("jane78", "jane@mail.com", "securepassword456");
			if (john == null || jane == null) {
				return 0;
			}
			ArticleDto spring = createArticle.execute(john, Map.of(
					"title", "Getting Started with Spring Boot",
					"description", "A short tour of building a REST API with Spring Boot",
					"body", "Spring Boot removes most of the boilerplate needed to ship a production ready service.",
					"tagList", List.of("spring", "java")));
			ArticleDto react = createArticle.execute(jane, Map.of(
					"title", "Understanding React Hooks",
					"description", "What hooks are and when to use them",
					"body", "Hooks let function components keep state and run side effects.",
					"tagList", List.of("react", "javascript")));
			addComment.execute(jane, spring.slug(), Map.of("body", "Great introduction, thanks for sharing."));
			addComment.execute(john, react.slug(), Map.of("body", "Very clear explanation of useEffect."));
			followUser.execute(jane, "johndoe123");
			log.info("Seeding completed");
			return 0;
		}
		catch (RuntimeException error) {
			log.error("Seeding failed: {}", error.getMessage(), error);
			return 1;
		}
	}

	private UUID registerOrSkip(String username, String email, String password) {
		try {
			UserDto user = registerUser.execute(Map.of("username", username, "email", email, "password", password));
			log.info("Seeded user {}", username);
			return verifyAccessToken.execute(user.token()).userId();
		}
		catch (AlreadyExistsException error) {
			log.warn("Skipped existing user {}", username);
			return null;
		}
	}
}
