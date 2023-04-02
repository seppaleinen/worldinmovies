# TMDB Import

### Mongo restore
```bash
docker cp worldinmovies_backend_1:/app/datadump.json worldinmovies_mongo_1:/

docker exec -ti worldinmovies_mongo_1 mongoimport -d tmdb -c movie --mode upsert --file datadump.json
```