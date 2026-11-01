CREATE TABLE articles (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  author_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  slug        VARCHAR(255) NOT NULL,
  title       VARCHAR(255) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  body        TEXT NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX articles_slug_unique ON articles (slug);
CREATE INDEX articles_author_created ON articles (author_id, created_at DESC);
CREATE INDEX articles_created ON articles (created_at DESC, id DESC);

CREATE TABLE article_tags (
  article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
  tag        VARCHAR(30) NOT NULL,
  position   SMALLINT NOT NULL,
  PRIMARY KEY (article_id, tag)
);

CREATE INDEX article_tags_tag ON article_tags (tag);
