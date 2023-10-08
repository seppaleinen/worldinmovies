FROM python:3.11-alpine as base

FROM base as builder

RUN mkdir /install
WORKDIR /install

RUN \
 apk update && \
 apk add --no-cache postgresql-libs tzdata libpq && \
 apk add --no-cache --virtual .build-deps gcc musl-dev postgresql-dev libffi-dev g++ snappy-dev krb5-pkinit krb5-dev krb5 && \
 pip install --upgrade pip

ADD requirements.txt .

RUN pip install --prefix=/install --no-cache-dir -r requirements.txt && \
    apk --purge del .build-deps

FROM base

ENV ENVIRONMENT docker
ENV PYTHONUNBUFFERED 1
ENV TZ Europe/Stockholm
ENV PIP_DISABLE_PIP_VERSION_CHECK 1
ENV PYTHONDONTWRITEBYTECODE 1

COPY --from=builder /install /usr/local
RUN apk --no-cache add libpq curl

ADD .. /app

WORKDIR /app

ENTRYPOINT ["/bin/sh", "-c", "python manage.py crontab add && crond & python manage.py makemigrations && python manage.py migrate && gunicorn --config=gunicorn.config.py -k uvicorn.workers.UvicornWorker --reload settings.asgi"]
