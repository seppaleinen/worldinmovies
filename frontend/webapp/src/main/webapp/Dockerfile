#FROM python:3.4-onbuild
FROM jfloff/alpine-python:3.4
ADD . /webapp

WORKDIR /webapp
RUN pip install -r requirements.txt