FROM openjdk:21-ea-jdk
COPY target/*.jar ./app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]