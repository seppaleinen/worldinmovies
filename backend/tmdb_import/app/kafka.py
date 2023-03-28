import datetime
import pickle, json
import threading

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from kafka import KafkaProducer, KafkaConsumer

data = []


def produce(event_type, message):
    producer = KafkaProducer(bootstrap_servers='127.0.0.1:9092',
                             value_serializer=lambda x: pickle.dumps(
                                 json.dumps({'movie_id': message, 'event': event_type}).encode('utf-8'),
                                 pickle.HIGHEST_PROTOCOL))
    producer.send('movie', message)


def kafka_consumer():
    print("Starting kafka-consumer")
    consumer = KafkaConsumer(
        'movie',
        bootstrap_servers=['localhost:9092'],
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        value_deserializer=lambda x: json.loads(pickle.loads(x)))
    for message in consumer:
        event = {"timestamp": datetime.datetime.now().isoformat(), "data": message.value}
        layer = get_channel_layer()
        async_to_sync(layer.group_send)('group', {"type": "events", "message": json.dumps(event)})

        global data
        data.append(event)
        data = data[:100]


if 'kafka_consumer' not in [thread.name for thread in threading.enumerate()]:
    thread = threading.Thread(target=kafka_consumer, name='kafka_consumer')
    thread.setDaemon(True)
    thread.start()


def get_data():
    return data