#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn gatling:test -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim

ENV simulation basicTest
ENV userCount 1
ENV rampDuration 1
ENV testDuration 1

COPY --from=build /home/app/target/gatling*.jar /usr/local/lib/gatling.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/gatling.jar"]
