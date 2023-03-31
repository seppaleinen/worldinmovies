import threading

from django.apps import AppConfig


class WorldinmoviesConfig(AppConfig):
    name = 'app'

    def ready(self):
        if 'kafka_consumer' not in [thread.name for thread in threading.enumerate()]:
            from app.kafka import kafka_consumer
            thread = threading.Thread(target=kafka_consumer, name='kafka_consumer')
            thread.daemon = True
            thread.start()
