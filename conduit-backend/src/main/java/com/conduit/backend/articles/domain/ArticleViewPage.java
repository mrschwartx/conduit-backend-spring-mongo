package com.conduit.backend.articles.domain;

import java.util.List;

public record ArticleViewPage(List<ArticleView> articles, long total) {
}
