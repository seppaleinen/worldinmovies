from flask import Flask
from raven.contrib.flask import Sentry
import os
app = Flask(__name__)
app.debug=True

app.config['SENTRY_RELEASE'] = os.environ.get('SENTRY_RELEASE', '0.0.1')
sentry = Sentry(app)
