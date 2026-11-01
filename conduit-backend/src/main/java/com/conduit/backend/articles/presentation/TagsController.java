package com.conduit.backend.articles.presentation;

import com.conduit.backend.articles.application.ListTags;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
public class TagsController {

	private final ListTags listTags;

	public TagsController(ListTags listTags) {
		this.listTags = listTags;
	}

	@GetMapping
	public Map<String, List<String>> list() {
		return Map.of("tags", listTags.execute());
	}
}
