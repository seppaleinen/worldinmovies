import json

from app.kafka import get_data
from channels.generic.websocket import AsyncWebsocketConsumer


class TextRoomConsumer(AsyncWebsocketConsumer):
    groupId = 'group'

    async def connect(self):
        await self.channel_layer.group_add(self.groupId, self.channel_name)
        await self.accept()
        await self.send(text_data=json.dumps(get_data()))

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(self.groupId, self.channel_name)

    async def receive(self, text_data):
        await self.channel_layer.group_send(self.groupId, {"type": "events", "message": json.dumps(text_data)})

    async def events(self, event):
        await self.send(text_data=event['message'])


