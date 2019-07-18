import os, sentry_sdk


sentry_url = os.getenv('SENTRY_URL')
if sentry_url:
    sentry_sdk.init(sentry_url)
