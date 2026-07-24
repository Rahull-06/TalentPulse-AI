# Build from repo root: docker build -f services/job-service/Dockerfile .
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/job-service/pom.xml .
COPY services/job-service/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd -r -u 10001 appuser
COPY --from=build /build/target/*-SNAPSHOT.jar /app/app.jar
COPY infra/docker/java-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh && chown appuser:appuser /app/app.jar /app/docker-entrypoint.sh
USER appuser
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
