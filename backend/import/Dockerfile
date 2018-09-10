FROM python:3-alpine

ENV PYTHONUNBUFFERED 0

ADD . /app

WORKDIR /app

RUN pip install --upgrade pip && \
    pip install --upgrade setuptools && \
    pip install -r requirements.txt