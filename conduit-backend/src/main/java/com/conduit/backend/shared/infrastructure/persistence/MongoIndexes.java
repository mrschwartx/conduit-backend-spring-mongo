package com.conduit.backend.shared.infrastructure.persistence;

import static com.conduit.backend.shared.infrastructure.persistence.MongoCollections.ARTICLES;
import static com.conduit.backend.shared.infrastructure.persistence.MongoCollections.COMMENTS;
import static com.conduit.backend.shared.infrastructure.persistence.MongoCollections.FAVORITES;
import static com.conduit.backend.shared.infrastructure.persistence.MongoCollections.FOLLOWS;
import static com.conduit.backend.shared.infrastructure.persistence.MongoCollections.USERS;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.conversions.Bson;

public final class MongoIndexes {

	private MongoIndexes() {
	}

	public static void ensure(MongoDatabase database) {
		unique(database, USERS, "users_email_unique", Indexes.ascending("emailLower"));
		unique(database, USERS, "users_username_unique", Indexes.ascending("usernameLower"));

		unique(database, FOLLOWS, "follows_pair", Indexes.ascending("followerId", "followeeId"));
		plain(database, FOLLOWS, "follows_followee", Indexes.ascending("followeeId"));

		unique(database, ARTICLES, "articles_slug_unique", Indexes.ascending("slug"));
		plain(database, ARTICLES, "articles_author_created", Indexes.compoundIndex(
				Indexes.ascending("authorId"), Indexes.descending("createdAt")));
		plain(database, ARTICLES, "articles_created", Indexes.compoundIndex(
				Indexes.descending("createdAt"), Indexes.descending("_id")));
		plain(database, ARTICLES, "articles_tags", Indexes.ascending("tags"));

		unique(database, FAVORITES, "favorites_pair", Indexes.ascending("userId", "articleId"));
		plain(database, FAVORITES, "favorites_article", Indexes.ascending("articleId"));

		plain(database, COMMENTS, "comments_article_created", Indexes.compoundIndex(
				Indexes.ascending("articleId"), Indexes.ascending("createdAt"), Indexes.ascending("_id")));
	}

	private static void unique(MongoDatabase database, String collection, String name, Bson keys) {
		database.getCollection(collection).createIndex(keys, new IndexOptions().name(name).unique(true));
	}

	private static void plain(MongoDatabase database, String collection, String name, Bson keys) {
		database.getCollection(collection).createIndex(keys, new IndexOptions().name(name));
	}
}
