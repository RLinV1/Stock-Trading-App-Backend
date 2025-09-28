FROM maven:3.9.11-amazoncorretto-24-al2023 AS build
WORKDIR /app

# Copy only needed files first for caching
COPY pom.xml .
COPY src ./src

# Build the jar
RUN mvn clean package -DskipTests


# Use a lightweight JDK base image
FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app.jar

# Command to run the jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
