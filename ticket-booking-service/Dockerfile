FROM openjdk:17
WORKDIR /app
COPY target/ticket-booking-service-0.0.1-SNAPSHOT.jar AuthService.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "AuthService.jar"]