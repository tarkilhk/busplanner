FROM openjdk:8-jdk-alpine

EXPOSE 8778

VOLUME /app
COPY target/*.jar /busplanner.jar

ENTRYPOINT ["java","-jar","/busplanner.jar"]