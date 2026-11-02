package com.conduit.backend.shared.infrastructure.persistence;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;

public final class DuplicateKeys {

	private DuplicateKeys() {
	}

	public static boolean isDuplicate(Throwable error) {
		return find(error) != null;
	}

	public static boolean violates(Throwable error, String index) {
		MongoException found = find(error);
		return found != null && found.getMessage() != null && found.getMessage().contains(index);
	}

	private static MongoException find(Throwable error) {
		for (Throwable current = error; current != null; current = current.getCause()) {
			if (current instanceof MongoException mongo
					&& ErrorCategory.fromErrorCode(mongo.getCode()) == ErrorCategory.DUPLICATE_KEY) {
				return mongo;
			}
			if (current.getCause() == current) {
				return null;
			}
		}
		return null;
	}
}
