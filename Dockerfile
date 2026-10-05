# Stage 1: build the jar with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Stage 2: run it on a small JRE image (no Maven, no source code)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/payrecon-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
