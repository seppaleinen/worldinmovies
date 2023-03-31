import datetime
import pickle
import os
import json

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from kafka import KafkaProducer, KafkaConsumer

kafka_url = 'kafka' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'localhost'
producer = KafkaProducer(bootstrap_servers=f"{kafka_url}:9092",
                         key_serializer=lambda x: pickle.dumps(x),
                         value_serializer=lambda x: pickle.dumps(x))


def produce(event_type, message):
    producer.send('movie', key=event_type, value=message)


def kafka_consumer():
    consumer = KafkaConsumer(
        'movie',
        bootstrap_servers=["%s:9092" % kafka_url],
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        key_deserializer=lambda x: pickle.loads(x),
        value_deserializer=lambda x: pickle.loads(x))
    layer = get_channel_layer()
    for message in consumer:
        event = {"timestamp": datetime.datetime.now().isoformat(), "event": message.key, "value": message.value}
        async_to_sync(layer.group_send)('group', {"type": "events", "message": json.dumps(event)})
