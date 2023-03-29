import datetime
import json
import pickle
import os
import threading

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from kafka import KafkaProducer, KafkaConsumer

kafka_url = 'kafka' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'localhost'


def produce(event_type, message):
    producer = KafkaProducer(bootstrap_servers="%s:9092" % kafka_url,
                             value_serializer=lambda x: pickle.dumps(
                                 json.dumps({'movie_id': message, 'event': event_type}).encode('utf-8'),
                                 pickle.HIGHEST_PROTOCOL))
    producer.send('movie', key=event_type, value=message)


def kafka_consumer():
    consumer = KafkaConsumer(
        'movie',
        bootstrap_servers=["%s:9092" % kafka_url],
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        value_deserializer=lambda x: json.loads(pickle.loads(x)))
    for message in consumer:
        event = {"timestamp": datetime.datetime.now().isoformat(), "data": message.value}
        layer = get_channel_layer()
        async_to_sync(layer.group_send)('group', {"type": "events", "message": json.dumps(event)})


if 'kafka_consumer' not in [thread.name for thread in threading.enumerate()]:
    thread = threading.Thread(target=kafka_consumer, name='kafka_consumer')
    thread.setDaemon(True)
    thread.start()