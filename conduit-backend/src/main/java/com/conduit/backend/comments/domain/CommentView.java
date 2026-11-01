package com.conduit.backend.comments.domain;

import com.conduit.backend.shared.domain.Profile;

public record CommentView(Comment comment, Profile author) {
}
