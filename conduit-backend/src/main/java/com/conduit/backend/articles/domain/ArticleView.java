package com.conduit.backend.articles.domain;

import com.conduit.backend.shared.domain.Profile;

public record ArticleView(Article article, Profile author, boolean favorited, long favoritesCount) {
}
