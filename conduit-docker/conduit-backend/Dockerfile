FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -ntp -q dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine AS runner
ENV APP_HOST=0.0.0.0 \
    APP_PORT=8080 \
    LOG_FILE=
WORKDIR /app
RUN addgroup -S conduit && adduser -S -G conduit conduit
COPY --from=build --chown=conduit:conduit /build/target/conduit-backend.jar ./app.jar
RUN chown -R conduit:conduit /app
USER conduit
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
