# Conduit Backend (Spring Boot, MongoDB)

The backend of Conduit, a Medium-style blogging platform built on the RealWorld API contract: sign up and sign in, edit your profile, follow other authors, publish articles with tags, favorite them, read a personal feed and discuss in comments. It is a REST API built with Spring Boot 4 and MongoDB, organised as a modular monolith with Domain-Driven Design and Clean Architecture.

The browser client is the separate Conduit React frontend. The API follows `conduit-openapi/openapi.yml`, the same contract the frontend and the other Conduit backends are written against.

**Stack:** Java 25 · Spring Boot 4 · Spring Web MVC · Spring Security · MongoDB 7 (Java sync driver) · JWT (jjwt) · bcrypt · JUnit 5 · Maven · Docker · GitHub Actions

## Contents

- [Quick start](#quick-start)
- [Features](#features)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Database](#database)
- [API](#api)
- [Contract decisions](#contract-decisions)
- [Testing](#testing)
- [CI](#ci)
- [Limits](#limits)
- [References](#references)

## Quick start

Requires JDK 25 and Docker.

```sh
cd conduit-docker
make docker/dev-up
cd ../conduit-backend
make env
make secret-key
make dev
```

`make env` copies `.env.example` to `.env`. Paste the value printed by `make secret-key` into `AUTH_SECRET` in `.env`. The API runs at http://localhost:8080/api/v1 and mongo-express at http://localhost:8081 (MongoDB user `dev_user`, password `dev_pass`). Collections and indexes are created on startup. The `.env` file is read from the working directory, so run the commands from `conduit-backend`, and real environment variables take precedence.

| Goal            | Command                                    |
| --------------- | ------------------------------------------ |
| Run             | `make dev`                                 |
| Build the jar   | `make build`                               |
| Run the jar     | `make run`                                 |
| Seed demo data  | `make seed`                                |
| Unit tests      | `make test`                                |
| All tests       | `make verify`                              |
| Clean           | `make clean`                               |
| Run with Docker | `make docker/up` (from `conduit-docker`)   |
| Seed in Docker  | `make docker/seed` (from `conduit-docker`) |
| Stop Docker     | `make docker/down` (from `conduit-docker`) |

The make targets call the Maven wrapper (`./mvnw`) and the jar `target/conduit-backend.jar`. `make seed` runs the jar with `--spring.main.web-application-type=none --conduit.seed.enabled=true`.

`make docker/up` creates `conduit-docker/.env` with a random database password and `AUTH_SECRET` on the first run, then starts MongoDB and the API and waits until both are healthy. `PORT` overrides the host port in the Docker setup. The container always listens on port 8080. Demo logins after seeding: `johndoe123@mail.com` / `password123` and `jane@mail.com` / `securepassword456`. Seeding skips quietly when a demo user already exists, and it refuses to run when `APP_LEVEL` is `production` unless `SEED_ALLOW_PRODUCTION` is `true`.

## Features

- **Accounts**: sign up, sign in, read and update the current user (username, email, password, bio, image).
- **Profiles**: public profiles, follow and unfollow.
- **Articles**: create, read, update and delete, with tags, generated slugs, favorites, filtering by tag, author and favoriter, pagination and a feed of followed authors.
- **Comments**: list, add and delete comments on an article.
- **Tags**: the 20 most used tags.
- **Platform**: collections and indexes created on startup, rolling log files, graceful shutdown, `GET /health` for health checks.

## Configuration

| Variable                | Default                 | Purpose                                                                            |
| ----------------------- | ----------------------- | ---------------------------------------------------------------------------------- |
| `APP_NAME`              | `conduit-api`           | Application name                                                                   |
| `APP_HOST`              | `0.0.0.0`               | Bind address                                                                       |
| `APP_PORT`              | `8080`                  | Port the API listens on                                                            |
| `APP_LEVEL`             | `development`           | `production` blocks seeding unless allowed                                         |
| `MONGODB_URI`           | required                | Connection string, `mongodb://dev_user:dev_pass@localhost:27017/?authSource=admin` |
| `MONGODB_DATABASE`      | `conduit`               | Database name, `conduit_dev` in `.env.example`                                     |
| `AUTH_SECRET`           | required                | JWT secret, at least 32 characters                                                 |
| `BCRYPT_ROUNDS`         | `10`                    | bcrypt cost factor, 4 to 31                                                        |
| `CLIENT_URL`            | `http://localhost:3000` | Allowed CORS origin                                                                |
| `LOG_LEVEL`             | `debug`                 | Level of the `com.conduit` packages                                                |
| `LOG_FILE`              | `logs/conduit-api.log`  | Log file, empty for console only                                                   |
| `SEED_ALLOW_PRODUCTION` | `false`                 | Allows seeding when `APP_LEVEL` is `production`                                    |

Access tokens live for 24 hours. The lifetime is `conduit.auth.token-ttl` in `application.yaml` and has no environment variable. The log file rolls at 20 MB and keeps 14 files. The Docker image sets `LOG_FILE` to empty, so containers log to the console only.

Docker only, in `conduit-docker/.env`:

| Variable      | Default                 | Purpose                                  |
| ------------- | ----------------------- | ---------------------------------------- |
| `PORT`        | `8080`                  | Host port used by Docker                 |
| `DB_NAME`     | `conduit`               | MongoDB database name                    |
| `DB_USER`     | `conduit`               | MongoDB root user                        |
| `DB_PASSWORD` | required                | MongoDB root password, generated by make |
| `AUTH_SECRET` | required                | JWT secret, generated by make            |
| `APP_LEVEL`   | `development`           | Environment level                        |
| `CLIENT_URL`  | `http://localhost:3000` | Allowed CORS origin                      |
| `LOG_LEVEL`   | `info`                  | Log level                                |

The frontend reaches the API through its own `/api` proxy (the Vite dev server or the nginx image), so no CORS setup is needed for the default stack. The backend still allows `CLIENT_URL` for direct cross-origin use.

## Architecture

### Project layout

```
conduit-backend/
  src/main/java/com/conduit/backend/
    config/      typed properties, security, adapter wiring, seed runner
    shared/      errors, ids, paging, profile read model, HTTP and persistence helpers
    users/       registration, login, current user, profiles, follows
    articles/    articles, tags, favorites, feed
    comments/    article comments
  src/main/resources/application.yaml
  src/test/      unit tests and integration tests (`*IT`)
conduit-docker/
  conduit-backend/Dockerfile
  docker-compose.local.yml
  docker-compose.dev.yml
  Makefile
conduit-openapi/openapi.yml
.github/workflows/ci.yml
.github/workflows/commitlint.yml
.commitlintrc.json
```

### Modules

```
<module>/
  domain/           entities, value objects, repository and query contracts
  application/      use cases
  infrastructure/   MongoDB, JWT, bcrypt
  presentation/     controllers
```

### Dependency rules

- Dependencies point inward: presentation and infrastructure depend on application, application depends on domain.
- Modules share only `shared`, including the `Profile` read model used for authors.
- Reads that need author, favorite and follow data go through `ArticleQueries` and `CommentQueries`, which batch their lookups to avoid N+1 queries.
- Authentication is cross-cutting: a servlet filter in `config` verifies the `Authorization: Token <jwt>` header and exposes the user id as `AuthenticatedUser`. Routes with optional auth work without a header but reject an invalid one.

## Database

There are no migration files. `MongoIndexes` creates the indexes idempotently on startup, and collections appear on first write. The folder `src/main/resources/db/migration` still holds SQL files from the PostgreSQL version, which nothing in this project loads.

| Collection  | Notes                                                                                            |
| ----------- | ------------------------------------------------------------------------------------------------ |
| `users`     | string UUID `_id`, `usernameLower` and `emailLower` carry the case-insensitive unique indexes    |
| `follows`   | unique `(followerId, followeeId)`, written with an idempotent upsert                             |
| `articles`  | string UUID `_id`, unique `slug`, tags embedded as an ordered `tags` array indexed for filtering |
| `favorites` | unique `(userId, articleId)`, written with an idempotent upsert                                  |
| `comments`  | numeric `_id` from the `counters` collection                                                     |
| `counters`  | `{_id: "comments", seq}` sequence used for comment ids                                           |

- Deleting an article also deletes its favorites and comments in the repository, without a transaction.
- Article listings are ordered by `createdAt` descending, then `_id` descending.

## API

All routes are under `/api/v1` and follow `conduit-openapi/openapi.yml`. Protected routes expect `Authorization: Token <jwt>`. Request bodies are wrapped (`{"user": {...}}`, `{"article": {...}}`, `{"comment": {...}}`).

| Method | Path                             | Auth     | Purpose                                                   |
| ------ | -------------------------------- | -------- | --------------------------------------------------------- |
| POST   | `/users`                         | none     | Register, returns 201                                     |
| POST   | `/users/login`                   | none     | Sign in                                                   |
| GET    | `/user`                          | required | Current user                                              |
| PUT    | `/user`                          | required | Update the current user                                   |
| GET    | `/profiles/{username}`           | optional | Get a profile                                             |
| POST   | `/profiles/{username}/follow`    | required | Follow a user                                             |
| DELETE | `/profiles/{username}/follow`    | required | Unfollow a user                                           |
| GET    | `/articles`                      | optional | List with `tag`, `author`, `favorited`, `offset`, `limit` |
| GET    | `/articles/feed`                 | required | Articles of followed authors                              |
| POST   | `/articles`                      | required | Create an article, returns 201                            |
| GET    | `/articles/{slug}`               | optional | Get an article                                            |
| PUT    | `/articles/{slug}`               | required | Update an own article                                     |
| DELETE | `/articles/{slug}`               | required | Delete an own article, returns 204                        |
| POST   | `/articles/{slug}/favorite`      | required | Favorite                                                  |
| DELETE | `/articles/{slug}/favorite`      | required | Unfavorite                                                |
| GET    | `/articles/{slug}/comments`      | optional | List comments, newest first                               |
| POST   | `/articles/{slug}/comments`      | required | Add a comment, returns 201                                |
| DELETE | `/articles/{slug}/comments/{id}` | required | Delete an own comment, returns 204                        |
| GET    | `/tags`                          | none     | The 20 most used tags                                     |

`GET /health` (outside `/api/v1`) returns the plain text `OK`. `GET /` returns a small welcome message.

Every non-2xx response (except `/health`) is `{"errors": {"<field-or-resource>": ["message"]}}`.

| Status | When                                                                                        |
| ------ | ------------------------------------------------------------------------------------------- |
| 400    | Malformed JSON: `body: is not valid JSON`                                                   |
| 401    | `token: is missing`, `is invalid` or `has expired`; wrong login: `credentials: are invalid` |
| 403    | Not the owner: `article` or `comment`: `forbidden`                                          |
| 404    | `article`, `profile` or `comment`: `not found`; unknown path: `path`                        |
| 409    | `username` or `email`: `has already been taken`                                             |
| 413    | `body: is too large`, the limit is 256 KB                                                   |
| 415    | `content-type: must be application/json`                                                    |
| 422    | Validation, all failing fields at once                                                      |
| 500    | `server: internal server error`                                                             |

## Contract decisions

Where the contract leaves room, the API behaves as follows.

- **Check order** on protected routes: authentication (401), existence (404), ownership (403), validation (422).
- **Authentication**: a token sent to an optional-auth route that is invalid or expired also yields `401`.
- **Validation** trims text and counts Unicode characters. `image` is empty, `null` or an `https` URL, and `null` or `""` clears `bio` and `image`.
- **`PUT /user` and `PUT /articles/{slug}`** are partial updates and need at least one known field, otherwise `user` or `article` is `can't be empty`.
- **Slugs** come from the title (NFKD, diacritics stripped, lowercase, at most 80 characters, fallback `article`). Collisions get `-2`, `-3`, `feed` is reserved, and a changed title regenerates the slug while an unchanged title keeps it.
- **Tags**: at most 10 after de-duplication, each 1 to 30 characters without whitespace or commas, in first-seen order.
- **Pagination**: lists are newest first, `limit` defaults to 20 and is clamped to 50, and `offset` and `limit` must be integers (at least 0 and at least 1) or the answer is `422`. Unknown `author` or `favorited` users give an empty list.
- **Following yourself** answers `422` with `username`.

| Field               | Rule                                                 |
| ------------------- | ---------------------------------------------------- |
| Request body        | up to 256 KB                                         |
| Username            | 3 to 30 characters, letters, digits, `_` and `-`     |
| Email               | valid format, up to 254 characters, stored lowercase |
| Password            | 8 to 72 characters                                   |
| Bio                 | up to 300 characters                                 |
| Image               | `https` URL                                          |
| Article title       | up to 100 characters                                 |
| Article description | up to 200 characters                                 |
| Article body        | up to 20000 characters                               |
| Comment body        | up to 1000 characters                                |

## Testing

| Layer       | Command       | What it proves                                                    |
| ----------- | ------------- | ----------------------------------------------------------------- |
| Unit        | `make test`   | Domain, use cases, JWT and bcrypt without I/O                     |
| Integration | `make verify` | Unit tests plus full HTTP flows on a real MongoDB (`*IT` classes) |

Integration tests read `TEST_MONGODB_URI` (default `mongodb://dev_user:dev_pass@localhost:27017/?authSource=admin`, the development MongoDB), use a temporary database and drop it on exit.

## CI

`.github/workflows/ci.yml` runs on pushes to `main` and on pull requests, with three jobs in sequence:

1. **conduit code build**: `./mvnw -DskipTests verify` with JDK 25.
2. **conduit code tests**: `./mvnw verify` against a MongoDB 7 service.
3. **conduit docker container**: builds the image, starts the Compose stack, then checks `/health`, registers a user through `POST /api/v1/users` and runs the seed.

`.github/workflows/commitlint.yml` is a separate workflow that lints commit messages on pull requests, configured in `.commitlintrc.json`.

## Limits

- The API needs a running MongoDB.
- Profile images are URLs. The API does not store or serve uploads.
- Access tokens are stateless, live for 24 hours and cannot be revoked.
- There is no account deletion endpoint, as the contract does not define one.
- Writes that touch several collections, such as deleting an article, are not transactional.
- Data from the former Taskly version is not migrated. Start with an empty database.

## References

- [Spring Boot](https://spring.io/projects/spring-boot) · [Spring Security](https://spring.io/projects/spring-security)
- [MongoDB Java sync driver](https://www.mongodb.com/docs/drivers/java/sync/current/)
- [jjwt](https://github.com/jwtk/jjwt)
- [RealWorld specification](https://realworld-docs.netlify.app)
- [Docker multi-stage builds](https://docs.docker.com/build/building/multi-stage/)
