package com.conduit.backend.integration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.UUID;

final class TestDatabase {

	static final TestDatabase INSTANCE = new TestDatabase();

	private static final String DEFAULT_URI = "mongodb://dev_user:dev_pass@localhost:27017/?authSource=admin";

	final String uri;

	final String name;

	private TestDatabase() {
		this.uri = System.getenv().getOrDefault("TEST_MONGODB_URI", DEFAULT_URI);
		this.name = "conduit_test_" + UUID.randomUUID().toString().replace("-", "");
		Runtime.getRuntime().addShutdownHook(new Thread(this::drop));
	}

	private void drop() {
		try (MongoClient client = MongoClients.create(uri)) {
			client.getDatabase(name).drop();
		}
		catch (RuntimeException ignored) {
			return;
		}
	}
}
