#!/bin/sh

### Dump postgres
docker exec -ti worldinmovies-db-1 pg_dump -U postgres postgres --clean --file=/tmp/dbexport.pgsql
docker cp worldinmovies-db-1:/tmp/dbexport.pgsql .

### Dump mongo
docker exec -ti worldinmovies-mongo-1 mongodump --out=/mongodump
docker cp worldinmovies-mongo-1:/mongodump .

### Dump neo4j
#docker exec -ti neo4j neo4j-admin database dump --expand-commands system --to-path=/backups && neo4j-admin database dump --expand-commands neo4j --to-path=/backups
#docker cp backups neo4j:/backups

### Start up new db images
docker compose up --build -d mongo db --remove-orphans


### Restore postgres
docker cp dbexport.pgsql postgres:/
docker exec -ti postgres psql -d postgres -U postgres -f /dbexport.pgsql

### Restore mongo
docker cp mongodump mongo:/
docker exec -ti mongo mongorestore /mongodump

### Restore neo4j
#docker cp backups neo4j:/backups
#docker exec -ti neo4j neo4j-admin database load --expand-commands system --from-path=/backups && neo4j-admin database load --expand-commands neo4j --from-path=/backups

### Start up all new images
docker compose up --build -d --remove-orphans

