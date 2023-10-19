# TMDB Import

Django backend keeping track of all data and updates from TMDB
This contains the base data that gets propagated to all other systems.

As soon as a movie gets fetched/updated/deleted, an event will be sent to kafka
containing the movie-id, and the event-type. Respective systems will decide if they
want to do something with this info, and fetch the data through http-requests

### Mongo restore
```bash
# The fashion way
docker exec -ti mongo mongodump --out=/mongodump
docker cp mongo:/mongodump .
docker cp mongodump mongo:/ 
docker exec -ti mongo mongorestore /mongodump

# The boomer way
docker exec -ti mongo mongoexport -d tmdb -c movie  --out datadump.json
docker cp mongo:/datadump.json .
docker cp datadump.json mongo:/ 
docker exec -ti mongo mongoimport -d tmdb -c movie --mode upsert --file datadump.json
```

```bash
# Generate new protobuf classes from proto-file
python3 -m grpc_tools.protoc -I./app/proto --python_out=./app/proto/ --grpc_python_out=./app/proto/ ./app/proto/movies.proto
```

```bash
# To run hypercorn
hypercorn --config hypercorn.config.toml settings.asgi:application

# Gunicorn
gunicorn --config=gunicorn.config.py -k uvicorn.workers.UvicornWorker --reload settings.asgi
```