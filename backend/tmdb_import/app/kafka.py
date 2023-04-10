import datetime
import os
import json

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from kafka import KafkaProducer, KafkaConsumer

kafka_url = 'kafka:9092' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'localhost:9093'
producer = KafkaProducer(bootstrap_servers=kafka_url,
                         key_serializer=lambda x: x.encode('utf-8'),
                         value_serializer=lambda x: repr(x).encode('utf-8'))


def produce(event_type, message, topic='movie'):
    producer.send(topic, key=event_type, value=message)


def kafka_consumer():
    consumer = KafkaConsumer(
        'movie',
        group_id="tmdb",
        bootstrap_servers=kafka_url,
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        key_deserializer=lambda x: x.decode('utf-8'),
        value_deserializer=lambda x: x.decode('utf-8'))
    layer = get_channel_layer()
    for message in consumer:
        event = {"timestamp": datetime.datetime.now().isoformat(), "event": message.key, "value": message.value}
        async_to_sync(layer.group_send)('group', {"type": "events", "message": json.dumps(event)})
