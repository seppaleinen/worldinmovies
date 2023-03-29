import json

from channels.generic.websocket import AsyncWebsocketConsumer

data = []


class TextRoomConsumer(AsyncWebsocketConsumer):
    groupId = 'group'

    async def connect(self):
        await self.channel_layer.group_add(self.groupId, self.channel_name)
        await self.accept()
        if data:
            await self.send(text_data=json.dumps(data))
        else:
            await self.send(text_data="Connected")

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(self.groupId, self.channel_name)

    async def receive(self, text_data):
        data.append(text_data)
        await self.channel_layer.group_send(self.groupId, {"type": "events", "message": json.dumps(text_data)})

    async def events(self, event):
        await self.send(text_data=event['message'])


