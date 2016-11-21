FROM maven:3.3-onbuild-alpine
ADD . /selenium

WORKDIR /selenium
ENTRYPOINT mvn clean install -Pselenium
#ENTRYPOINT tail -f Dockerfile