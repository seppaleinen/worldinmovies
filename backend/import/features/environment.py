from pretenders.client.http import HTTPMock
import os
import threading
import time
from pretenders.server import server


def start_server():
    server.run(host='localhost', port=8000)


def before_all(context):
    t = context.t = threading.Thread(target=start_server)
    t.daemon = True
    t.start()
    time.sleep(1)

    mock = context.mock = HTTPMock('localhost', 8000)
    os.environ["tmdb_url"] = 'http://localhost:8000'

