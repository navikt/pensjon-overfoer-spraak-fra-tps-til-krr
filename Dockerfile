FROM eclipse-temurin:21
WORKDIR /application
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
