FROM python:3-alpine as base

FROM base as builder

RUN mkdir /install
WORKDIR /install

RUN \
 apk update && \
 apk add --no-cache postgresql-libs tzdata libpq && \
 apk add --no-cache --virtual .build-deps gcc musl-dev postgresql-dev

ADD ./requirements.txt .

RUN pip install --prefix=/install --no-cache-dir -r requirements.txt && \
    apk --purge del .build-deps

FROM base

ENV ENVIRONMENT DOCKER
ENV PYTHONUNBUFFERED 1
ENV TZ Europe/Stockholm

COPY --from=builder /install /usr/local
RUN apk --no-cache add libpq

ADD . /app

WORKDIR /app

ENTRYPOINT ["/bin/sh", "-c", "python manage.py crontab add && crond & python manage.py makemigrations && python manage.py migrate && gunicorn --config=gunicorn.config.py settings.wsgi"]
