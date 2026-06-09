FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S orderplatform && adduser -S orderplatform -G orderplatform

COPY target/spring-modulith-order-platform-0.1.0-SNAPSHOT.jar app.jar

USER orderplatform

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
