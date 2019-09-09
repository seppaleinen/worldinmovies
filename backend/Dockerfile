FROM python:3-alpine

RUN \
 apk add --no-cache postgresql-libs tzdata && \
 apk add --no-cache --virtual .build-deps gcc musl-dev postgresql-dev

ENV ENVIRONMENT DOCKER
ENV PYTHONUNBUFFERED 1
ENV TZ Europe/Stockholm

ADD ./requirements.txt .

RUN pip install  --no-cache-dir -r requirements.txt && \
    apk --purge del .build-deps

ADD . /app

WORKDIR /app

RUN python manage.py crontab add

ENTRYPOINT ["/bin/sh", "-c", " crond & python manage.py makemigrations && python manage.py migrate && gunicorn --config=gunicorn.config.py settings.wsgi"]
