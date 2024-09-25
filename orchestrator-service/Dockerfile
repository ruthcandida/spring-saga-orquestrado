FROM openjdk:21-ea-jdk
COPY target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]