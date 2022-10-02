FROM openjdk:8

EXPOSE 8778

WORKDIR /app

COPY target/busplanner-0.0.1-SNAPSHOT.jar /applications/busplanner.jar

ENTRYPOINT ["java","-jar", "busplanner.jar"]