FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY apps/api/pom.xml apps/api/pom.xml
RUN mvn -f apps/api/pom.xml -DskipTests dependency:go-offline

COPY apps/api apps/api
RUN mvn -f apps/api/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV PORT=8080

COPY --from=build /workspace/apps/api/target/devbrief-api-0.0.1-SNAPSHOT.jar /app/app.jar
COPY apps/api/docker-entrypoint.sh /app/docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["sh", "/app/docker-entrypoint.sh"]
