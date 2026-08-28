FROM eclipse-temurin:21-jre
ADD bot.jar /bot.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dfile.encoding=UTF-8","-jar","/bot.jar"]