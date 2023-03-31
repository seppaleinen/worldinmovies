import pickle
import os
import requests

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from kafka import KafkaConsumer
from app.models import Movie

kafka_url = 'kafka' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'localhost'
tmdb_url = 'tmdb_import' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'http://localhost:8020'


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
        event_type = message.key
        movie_id = message.value
        event = f"Processing {event_type} with id={movie_id}"
        if event_type == 'NEW' or event_type == 'UPDATE':
            response = requests.get(f"{tmdb_url}/movie/{movie_id}")
            if response.status_code == 200:
                try:
                    movie = Movie.objects.get(pk=movie_id)
                except Movie.DoesNotExist:
                    movie = Movie(id=movie_id, fetched=False)
                movie.add_fetched_info(response.json()[0])
                movie.save()
        elif event_type == 'DELETE':
            Movie.objects.get(pk=movie_id).delete()

        async_to_sync(layer.group_send)('group', {"type": "events", "message": event})