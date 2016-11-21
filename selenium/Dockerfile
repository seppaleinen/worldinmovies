FROM maven:3.3-onbuild-alpine
ADD . /selenium

WORKDIR /selenium
ENTRYPOINT mvn clean install -DskipTests=false
#ENTRYPOINT tail -f Dockerfile