import datetime

from channels.generic.websocket import AsyncWebsocketConsumer
from websockets.exceptions import ConnectionClosedOK

data = []


class Consumer(AsyncWebsocketConsumer):
    groupId = 'group'

    """
    Helper method
    """
    async def send_data(self, text_data):
        event = f"{datetime.datetime.now()} - BASE - {text_data}"
        data.append(event)
        try:
            await self.send(text_data=event)
        except ConnectionClosedOK:
            print("Client has disconnected from ws. Ignoring")

    async def connect(self):
        await self.channel_layer.group_add(self.groupId, self.channel_name)
        await self.accept()
        if data:
            for i in data:
                await self.send(text_data=i)
        else:
            await self.send_data("Connected")

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(self.groupId, self.channel_name)

    async def receive(self, text_data):
        data.append(text_data)
        await self.channel_layer.group_send(self.groupId, {"type": "events", "message": text_data})

    """
    Triggers on groupsends to type: events
    """
    async def events(self, event):
        print("SCOPE: %s" % self.scope)
        await self.send_data(event['message'])



