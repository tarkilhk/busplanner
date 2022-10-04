FROM openjdk:8-jdk-alpine

EXPOSE 8778

#VOLUME /busplanner
COPY target/*.jar /busplanner/busplanner.jar

ENTRYPOINT ["java","-jar","/busplanner/busplanner.jar"]