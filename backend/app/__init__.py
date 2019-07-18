import os, sentry_sdk
from sentry_sdk.integrations.django import DjangoIntegration


sentry_url = os.getenv('SENTRY_URL')
if sentry_url:
    sentry_sdk.init(sentry_url, integrations=[DjangoIntegration()])
