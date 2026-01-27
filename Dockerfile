# Build Stage
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# Run Stage
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx180m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
