package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.PageWindow;

public record ArticleFilter(String tag, String author, String favoritedBy, PageWindow window) {
}
