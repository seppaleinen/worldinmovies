import os

from channels.routing import ProtocolTypeRouter, URLRouter
from django.core.asgi import get_asgi_application
from django.urls import path
from app.websocket import TextRoomConsumer

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "settings.settings")

websocket_urlpatterns = [
    path('ws/', TextRoomConsumer.as_asgi()),
]

application = ProtocolTypeRouter(
    {
        "http": get_asgi_application(),
        'websocket': URLRouter(websocket_urlpatterns),
    }
)

