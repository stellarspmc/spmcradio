FROM eclipse-temurin:21-jre
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]