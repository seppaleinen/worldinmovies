FROM python:3.11-slim as base

FROM base as builder

RUN mkdir /install
WORKDIR /install

RUN \
 apt-get update -y && \
 apt-get install -y tzdata gcc libffi-dev g++ krb5-pkinit libsnappy-dev libkrb5-dev

ADD requirements.txt .

RUN pip install --prefix=/install --no-cache-dir -r requirements.txt

FROM base

ENV ENVIRONMENT docker
ENV PYTHONUNBUFFERED 1
ENV TZ Europe/Stockholm
ENV PIP_DISABLE_PIP_VERSION_CHECK 1
ENV PYTHONDONTWRITEBYTECODE 1

COPY --from=builder /install /usr/local
RUN apt-get update -y && apt-get install -y libpq-dev curl

ADD .. /app

WORKDIR /app

ENTRYPOINT ["/bin/sh", "-c", "python manage.py crontab add && crond & gunicorn --config=gunicorn.config.py -k uvicorn.workers.UvicornWorker --reload settings.asgi"]
