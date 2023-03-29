import os

from channels.auth import AuthMiddlewareStack
from channels.routing import ProtocolTypeRouter, URLRouter, ChannelNameRouter
from channels.security.websocket import AllowedHostsOriginValidator
from django.core.asgi import get_asgi_application
from django.urls import re_path
from app.websocket import TextRoomConsumer

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
        "channel": ChannelNameRouter({
            "thumbnails-generate": TextRoomConsumer.as_asgi(),
        }),
    }
)

