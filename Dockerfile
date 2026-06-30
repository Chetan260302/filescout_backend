# Stage 1 - Build (Using Java 21 JDK)
FROM maven:3.9-eclipse-temurin-21 as build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests


# Stage 2 - Runtime (Using Java 21 Alpine JRE for a tiny image)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]