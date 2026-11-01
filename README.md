# Conduit Backend (Spring Boot)

A RealWorld ("Conduit") backend: register and log in, edit your profile, follow authors, publish articles with tags, favorite articles and comment on them. It is a REST API built with Spring Boot 4 and PostgreSQL, organised as a modular monolith with Domain-Driven Design and Clean Architecture. The HTTP contract is defined by [`conduit-openapi/openapi.yml`](conduit-openapi/openapi.yml).

**Stack:** Java 25 · Spring Boot 4 · Spring Web MVC · Spring Security · Spring Data JPA · PostgreSQL 18 · Flyway · JWT (jjwt) · bcrypt · JUnit 5 · Docker · GitHub Actions

## Quick start

Requires JDK 25 and Docker.

```sh
cd conduit-docker
make docker/dev-up
cd ../conduit-backend
cp .env.example .env
openssl rand -base64 32
./mvnw spring-boot:run
```

Paste the generated value into `AUTH_SECRET` in `.env`. The API runs at http://localhost:8080/api/v1 and pgAdmin at http://localhost:8081 (login `admin@conduit.dev` / `admin`, server host `conduit-postgres`, user `dev_user`, password `dev_pass`). Flyway migrations run on startup.

| Goal            | Command                                                                                                  |
| --------------- | -------------------------------------------------------------------------------------------------------- |
| Run             | `./mvnw spring-boot:run`                                                                                 |
| Build the jar   | `./mvnw -DskipTests package`                                                                             |
| Seed demo data  | `java -jar target/conduit-backend.jar --spring.main.web-application-type=none --conduit.seed.enabled=true` |
| Run with Docker | `make docker/up` (from `conduit-docker`)                                                                 |
| Seed in Docker  | `make docker/seed` (from `conduit-docker`)                                                               |

Demo login after seeding: `johndoe123@mail.com` / `password123`.

## Configuration

| Variable                | Default                 | Purpose                                         |
| ----------------------- | ----------------------- | ----------------------------------------------- |
| `APP_NAME`              | `conduit-api`           | Application name                                |
| `APP_HOST` / `APP_PORT` | `0.0.0.0` / `8080`      | Bind address and port                           |
| `APP_LEVEL`             | `development`           | `production` blocks seeding unless allowed      |
| `DATABASE_URL`          | required                | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/conduit_dev` |
| `DATABASE_USER`         | required                | PostgreSQL user                                 |
| `DATABASE_PASSWORD`     | required                | PostgreSQL password                             |
| `DATABASE_POOL_MAX`     | `10`                    | Maximum pool size                               |
| `DATABASE_TIMEOUT`      | `30000`                 | Connection timeout in ms                        |
| `AUTH_SECRET`           | required                | JWT secret, at least 32 characters              |
| `BCRYPT_ROUNDS`         | `10`                    | bcrypt cost factor                              |
| `CLIENT_URL`            | `http://localhost:3000` | Allowed CORS origin                             |
| `LOG_LEVEL`             | `debug`                 | Level of the `com.conduit` packages             |
| `LOG_FILE`              | `logs/conduit-api.log`  | Log file, empty for console only                |
| `SEED_ALLOW_PRODUCTION` | `false`                 | Allows seeding when `APP_LEVEL` is `production` |

Docker only (`conduit-docker/.env`): `PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `AUTH_SECRET`, `CLIENT_URL`, `LOG_LEVEL`.

The React frontend (`conduit-frontend-react`) talks to `http://localhost:8080/api/v1` through its Vite or nginx `/api` proxy, so no CORS setup is needed; `CLIENT_URL` only matters for direct cross-origin calls.

## Architecture

```
conduit-backend/src/main/java/com/conduit/backend/
  config/      typed properties, security, adapter wiring, seed runner
  shared/      errors, ids, paging, profile read model, HTTP and persistence helpers
  users/       registration, login, current user, profiles, follows
  articles/    articles, tags, favorites, feed
  comments/    article comments
```

Each module has `domain/` (entities, value objects, repository and query contracts), `application/` (use cases), `infrastructure/` (JPA, JWT, bcrypt) and `presentation/` (controllers).

- Dependencies point inward. Modules share only `shared`, including the `Profile` read model used for authors.
- Reads that need author, favorite and follow data go through `ArticleQueries` and `CommentQueries`, which batch their lookups to avoid N+1 queries.
- Authentication is cross-cutting: a servlet filter in `config` verifies the `Authorization: Token <jwt>` header and exposes the user id as `AuthenticatedUser`. Routes with optional auth work without a header but reject an invalid one.

## Database

Flyway migrations live in `src/main/resources/db/migration`.

| Table          | Notes                                                                        |
| -------------- | ---------------------------------------------------------------------------- |
| `users`        | UUID `id`, case-insensitive unique `email` and `username`, nullable `bio` and `image` |
| `follows`      | `(follower_id, followee_id)` primary key, self-follow blocked by `CHECK`     |
| `articles`     | UUID `id`, unique `slug`, `author_id` with `ON DELETE CASCADE`               |
| `article_tags` | `(article_id, tag)` primary key                                              |
| `favorites`    | `(user_id, article_id)` primary key                                          |
| `comments`     | identity `id`, cascades with its article and author                          |

## API

All routes are under `/api/v1`. Protected routes need `Authorization: Token <jwt>`.

| Method      | Path                               | Auth     | Purpose                          |
| ----------- | ---------------------------------- | -------- | -------------------------------- |
| POST        | `/users`                           | none     | Register (201)                   |
| POST        | `/users/login`                     | none     | Log in                           |
| GET, PUT    | `/user`                            | required | Current user, update             |
| GET         | `/profiles/{username}`             | optional | Get a profile                    |
| POST, DELETE| `/profiles/{username}/follow`      | required | Follow, unfollow                 |
| GET         | `/articles`                        | optional | List, filter by `tag`, `author`, `favorited`, page with `offset` and `limit` |
| GET         | `/articles/feed`                   | required | Articles of followed authors     |
| POST        | `/articles`                        | required | Create (201)                     |
| GET         | `/articles/{slug}`                 | optional | Get an article                   |
| PUT, DELETE | `/articles/{slug}`                 | required | Update, delete (204)             |
| GET         | `/articles/{slug}/comments`        | optional | List comments                    |
| POST        | `/articles/{slug}/comments`        | required | Add a comment (201)              |
| DELETE      | `/articles/{slug}/comments/{id}`   | required | Delete a comment (204)           |
| POST, DELETE| `/articles/{slug}/favorite`        | required | Favorite, unfavorite             |
| GET         | `/tags`                            | none     | List tags                        |

Request bodies are wrapped (`{"user": {...}}`, `{"article": {...}}`, `{"comment": {...}}`). `GET /health` (outside the versioned prefix) returns plain text `OK`.

### Errors

Every non-2xx response (except 204 and `/health`) is `{"errors": {"<field-or-resource>": ["message"]}}`.

| Status | When                                                                                                  |
| ------ | ----------------------------------------------------------------------------------------------------- |
| 400    | Malformed JSON: `errors.body = ["is not valid JSON"]`                                                  |
| 401    | `errors.token` is `is missing`, `is invalid` or `has expired`; wrong login: `errors.credentials = ["are invalid"]` |
| 403    | Not the owner: `errors.article` or `errors.comment = ["forbidden"]`                                    |
| 404    | `errors.article`, `errors.profile`, `errors.comment` = `["not found"]`; unknown path: `errors.path`    |
| 409    | `errors.username` or `errors.email = ["has already been taken"]`                                       |
| 415    | A request body that is not `application/json`                                                          |
| 422    | Validation, all failing fields at once                                                                 |
| 500    | `errors.server = ["internal server error"]`                                                            |

Protected endpoints check authentication (401), then existence (404), ownership (403) and validation (422), in that order. A token that is sent to an optional-auth route but is invalid or expired also yields 401.

### Rules

- Text is trimmed and measured in Unicode characters. `username` 3-30 of `[A-Za-z0-9_-]`, unique ignoring case; `email` valid, stored lowercase, unique; `password` 8-72; `bio` up to 300; `image` empty, `null` or an https URL (`null` or `""` clears `bio` and `image`).
- `title` up to 100, `description` up to 200, `body` up to 20000, comment `body` up to 1000. `tagList` has at most 10 tags after de-duplication, each 1-30 characters without whitespace or commas, in first-seen order.
- Slugs come from the title (NFKD, diacritics stripped, lowercase, at most 80 characters, fallback `article`). Collisions get `-2`, `-3`, and `feed` is reserved. Changing the title regenerates the slug; an unchanged title keeps it.
- `PUT /user` and `PUT /articles/{slug}` are partial updates and need at least one known field (`errors.user` or `errors.article = ["can't be empty"]`).
- Lists are newest first. `limit` defaults to 20 and is clamped to 50; `offset` and `limit` must be integers (>= 0 and >= 1) or the response is 422. Unknown `author` or `favorited` users give an empty list.
- `/tags` returns the 20 most used tags. Comments are listed newest first. Following yourself gives 422 (`errors.username`).

## Testing

| Layer       | Command         | What it proves                                          |
| ----------- | --------------- | ------------------------------------------------------- |
| Unit        | `./mvnw test`   | Domain, use cases, JWT and bcrypt without I/O           |
| Integration | `./mvnw verify` | Full HTTP flows on a real PostgreSQL (`*IT` classes)    |

Integration tests read `TEST_DATABASE_URL` (default: the development database), create a temporary database, run the migrations and drop it on exit.

## CI

`.github/workflows/ci.yml` builds with JDK 25, runs unit and integration tests against PostgreSQL 18, then builds the Docker image, checks `/health`, registers a user through `POST /api/v1/users` and runs the seed.
