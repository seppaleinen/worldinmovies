# Database scripts

```
docker exec -ti worldinmovies_mongo_1 /bin/bash
mongoimport -d worldinmovies -c CountryEntity --type csv --file countries_base.csv --headerline
```
