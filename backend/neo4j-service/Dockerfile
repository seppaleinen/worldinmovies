#FROM maven:3-amazoncorretto-21-al2023 as build
FROM maven:3-amazoncorretto-17 as build

WORKDIR /

ADD pom.xml .
ADD settings.xml .
ADD src src

RUN find src/

RUN mvn -s settings.xml clean install --no-transfer-progress -DskipTests

#FROM amazoncorretto:21-al2023-jdk as prod
#FROM amazoncorretto:21-al2023-headless as prod
FROM amazoncorretto:17-al2023 as prod

ENV TZ Europe/Stockholm

COPY --from=build /target/*.jar /target.jar

ENTRYPOINT ["java", "-jar", "/target.jar"]