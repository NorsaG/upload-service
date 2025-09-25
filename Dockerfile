FROM openjdk:17-alpine

WORKDIR /app
COPY target/upload-service-*.jar app.jar
EXPOSE 8089

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
