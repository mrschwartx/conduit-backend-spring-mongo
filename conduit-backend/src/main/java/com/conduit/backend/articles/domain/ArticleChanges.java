package com.conduit.backend.articles.domain;

public record ArticleChanges(Title title, Description description, Body body, Tags tags) {

	public boolean isEmpty() {
		return title == null && description == null && body == null && tags == null;
	}
}
