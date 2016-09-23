from flask import Flask
from raven.contrib.flask import Sentry
import os
app = Flask(__name__)
app.debug=True

app.config['SENTRY_RELEASE'] = os.environ.get('SENTRY_RELEASE', '0.0.1')
sentry = Sentry(app, dsn='https://34ce8ef3cc3740339cac1d70afae7d54:33f2eb87d7d8446cab3564f87d2be8fb@sentry.io/100439')
