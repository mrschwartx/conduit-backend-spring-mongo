package com.conduit.backend.config;

import com.conduit.backend.shared.infrastructure.persistence.MongoIndexes;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

	@Bean(destroyMethod = "close")
	MongoClient mongoClient(ConduitProperties properties) {
		return MongoClients.create(properties.mongo().uri());
	}

	@Bean
	MongoDatabase mongoDatabase(MongoClient client, ConduitProperties properties) {
		MongoDatabase database = client.getDatabase(properties.mongo().database());
		MongoIndexes.ensure(database);
		return database;
	}
}
