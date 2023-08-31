FROM openjdk:17
VOLUME /tmp
ARG JAR_FILE=target/notification-service.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]