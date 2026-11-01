MVN = ./mvnw
JAR = target/conduit-backend.jar
SEED_ARGS = --spring.main.web-application-type=none --conduit.seed.enabled=true

.PHONY: env secret-key dev build run test verify seed clean

env:
	@test -f .env || { cp .env.example .env && echo "Created .env from .env.example"; }

secret-key:
	@openssl rand -base64 32

dev:
	$(MVN) spring-boot:run

build:
	$(MVN) -DskipTests package

run: build
	java -jar $(JAR)

test:
	$(MVN) test

verify:
	$(MVN) verify

seed: build
	java -jar $(JAR) $(SEED_ARGS)

clean:
	$(MVN) clean