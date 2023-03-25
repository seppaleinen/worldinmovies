import pickle, json

from kafka import KafkaProducer


def produce(event_type, message):
    producer = KafkaProducer(bootstrap_servers='127.0.0.1:9092')
    v = json.dumps({'movie_id': message, 'event': event_type})
    serialized_data = pickle.dumps(v, pickle.HIGHEST_PROTOCOL)
    producer.send('movie', serialized_data)
