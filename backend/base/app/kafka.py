import os
import requests

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from kafka import KafkaConsumer
from app.models import Movie
from urllib3.util.retry import Retry
from requests.adapters import HTTPAdapter
from django.db import transaction
from collections import defaultdict

kafka_url = 'kafka:9092' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'localhost:9093'
tmdb_url = 'http://tmdb:8020' if os.getenv('ENVIRONMENT', 'docker') == 'docker' else 'http://localhost:8020'


def kafka_consumer():
    consumer = KafkaConsumer(
        'movie',
        bootstrap_servers=[kafka_url],
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        key_deserializer=lambda x: x.decode('utf-8'),
        value_deserializer=lambda x: x.decode('utf-8'))
    layer = get_channel_layer()
    while True:
        for events in consumer.poll(timeout_ms=10000, max_records=25).values():
            grouped = defaultdict(list)
            for event in events:
                grouped[event.key].append(event.value)
            for key, values in grouped.items():
                if key == 'NEW' or key == 'UPDATE':
                    session = requests.Session()
                    retry = Retry(connect=3, backoff_factor=2)
                    adapter = HTTPAdapter(max_retries=retry)
                    session.mount('http://', adapter)
                    session.mount('https://', adapter)
                    ids = ",".join(str(x) for x in values)
                    response = session.get(f"{tmdb_url}/movie/{ids}", timeout=5)
                    if response.status_code == 200:
                        found = dict()
                        for x in response.json():
                            found[x["id"]] = x
                        with transaction.atomic():
                            found_ids = Movie.objects.filter(pk__in=values).distinct().values_list('id', flat=True)
                            new_movie_ids = []
                            for new_id in values:
                                if str(new_id) not in [str(found_id) for found_id in found_ids]:
                                    new_movie_ids.append(x["id"])
                            for new_movie_id in new_movie_ids:
                                movie = Movie(id=new_movie_id, fetched=True)
                                movie.add_fetched_info(found[new_movie_id])
                            for movie in Movie.objects.filter(pk__in=found_ids):
                                movie.add_fetched_info(found[movie.id])
                                movie.save()
                elif key == 'DELETE':
                    Movie.objects.filter(pk__in=values).delete()

            async_to_sync(layer.group_send)('group', {"type": "events", "message": grouped})
