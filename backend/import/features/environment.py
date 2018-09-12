from wiremock.server import WireMockServer
from wiremock.constants import Config
import os


def before_all(context):
    wm = context.wiremock_server = WireMockServer()
    wm.start()
    Config.base_url = 'http://localhost:{}/__admin'.format(wm.port)
    os.environ["tmdb_url"] = 'http://localhost:{}'.format(wm.port)


def after_all(context):
    context.wiremock_server.stop()

