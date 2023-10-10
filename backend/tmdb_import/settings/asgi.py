import os

from channels.auth import AuthMiddlewareStack
from channels.routing import ProtocolTypeRouter, URLRouter
from channels.security.websocket import AllowedHostsOriginValidator
from django.core.asgi import get_asgi_application
from django.urls import re_path

from app.proto import movies_pb2_grpc
from app.proto.movies_pb2_grpc import MoviesController
from app.websocket import TextRoomConsumer
from sonora.asgi import grpcASGI

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "settings.settings")

websocket_urlpatterns = [
    re_path('^ws*', TextRoomConsumer.as_asgi()),
]

application = ProtocolTypeRouter(
    {
        "http": get_asgi_application(),
        'websocket': AllowedHostsOriginValidator(
            AuthMiddlewareStack(URLRouter(websocket_urlpatterns))
        ),
    }
)

application.asgi_app = grpcASGI(get_asgi_application())
movies_pb2_grpc.add_MoviesControllerServicer_to_server(MoviesController(), application.asgi_app)


