FROM eclipse-temurin:23
WORKDIR /application
COPY target/*.jar app.jar

ENV LOGGING_CONFIG=classpath:logback-nais.xml

ENTRYPOINT ["java", "-jar", "app.jar"]
