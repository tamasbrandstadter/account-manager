FROM adoptopenjdk/openjdk15:latest

EXPOSE 8080

ARG JAR_FILE=build/libs/accountmanager-0.0.1-MVP.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]
