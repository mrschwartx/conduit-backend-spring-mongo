package com.conduit.backend.shared.infrastructure.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class Sequences {

	private final MongoCollection<Document> counters;

	public Sequences(MongoDatabase database) {
		this.counters = database.getCollection(MongoCollections.COUNTERS);
	}

	public long next(String name) {
		Document counter = counters.findOneAndUpdate(
				Filters.eq("_id", name),
				Updates.inc("seq", 1L),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
		return ((Number) counter.get("seq")).longValue();
	}
}
