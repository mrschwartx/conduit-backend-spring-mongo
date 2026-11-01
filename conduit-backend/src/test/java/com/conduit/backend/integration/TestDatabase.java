package com.conduit.backend.integration;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

final class TestDatabase {

	static final TestDatabase INSTANCE = new TestDatabase();

	private static final String DEFAULT_URL = "postgres://dev_user:dev_pass@localhost:5432/conduit_dev";

	final String jdbcUrl;

	final String username;

	final String password;

	private final String adminJdbcUrl;

	private final String databaseName;

	private TestDatabase() {
		String raw = System.getenv().getOrDefault("TEST_DATABASE_URL", DEFAULT_URL);
		URI uri = URI.create(raw);
		String[] credentials = (uri.getUserInfo() == null ? "" : uri.getUserInfo()).split(":", 2);
		this.username = decode(credentials[0]);
		this.password = credentials.length > 1 ? decode(credentials[1]) : "";
		int port = uri.getPort() == -1 ? 5432 : uri.getPort();
		String prefix = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/";
		this.adminJdbcUrl = prefix + uri.getPath().substring(1);
		this.databaseName = "conduit_test_" + UUID.randomUUID().toString().replace("-", "");
		this.jdbcUrl = prefix + databaseName;
		execute("CREATE DATABASE " + databaseName);
		Runtime.getRuntime().addShutdownHook(new Thread(this::drop));
	}

	private void drop() {
		try {
			execute("DROP DATABASE IF EXISTS " + databaseName + " WITH (FORCE)");
		}
		catch (RuntimeException ignored) {
			return;
		}
	}

	private void execute(String sql) {
		try (Connection connection = DriverManager.getConnection(adminJdbcUrl, username, password);
				Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
		catch (SQLException error) {
			throw new IllegalStateException("Test database setup failed: " + error.getMessage(), error);
		}
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}
}
