FROM python:3-alpine as base

FROM base as builder

RUN mkdir /install
WORKDIR /install

RUN \
 apk update && \
 apk add --no-cache tzdata  && \
 apk add --no-cache --virtual .build-deps gcc g++ snappy-dev krb5-pkinit krb5-dev krb5

ADD requirements.txt .

RUN pip install --prefix=/install --no-cache-dir -r requirements.txt

FROM base

ENV ENVIRONMENT DOCKER
ENV PYTHONUNBUFFERED 1
ENV TZ Europe/Stockholm

COPY --from=builder /install /usr/local
RUN apk --no-cache add libpq curl

ADD .. /app

WORKDIR /app

ENTRYPOINT ["/bin/sh", "-c", "python manage.py crontab add && crond & gunicorn --config=gunicorn.config.py -k uvicorn.workers.UvicornWorker --reload settings.asgi"]
