FROM maven:3.3-jdk-8
ADD . /selenium

WORKDIR /selenium
ENTRYPOINT mvn clean install -DskipTests=false
#ENTRYPOINT tail -f Dockerfile