FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app/app.jar
ENTRYPOINT ["java","-jar","app/app.jar"]
EXPOSE 8080
