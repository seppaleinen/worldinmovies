# Backend

This part will be the backend handling imports and serving data to the front-end.

In future, this might be separated into different backend systems but for now will be one big chunk.


Basically will be handling
* Import
  - Daily file export from TMDB with all movie ids that are available
  - Fetching movie data from TMDB, dependent on the movie ids received from the daily file
* Serving Frontend


### Notes
* To fetch images, prefix with: https://image.tmdb.org/t/p/w500/

### DB Migration Guide
```bash
docker exec -ti worldinmovies_db_1 pg_dump -U postgres postgres --clean --file=/tmp/dbexport.pgsql

docker cp worldinmovies_db_1:/tmp/dbexport.pgsql .

rsync --progress dbexport.pgsql <ssh-server>:<path>

ssh <ssh-server>

docker cp dbexport.pgsql worldinmovies_db_1:/

docker exec -ti worldinmovies_db_1 psql -d postgres -U postgres -f /tmp/dbexport.pgsql
```

Installation
```bash
# Install requirements
pip3 install -r requirements

# To create and update database
./manage.py makemigrations && ./manage.py migrate

# To start server with gunicorn
gunicorn --config=gunicorn.config.py -k uvicorn.workers.UvicornWorker --reload settings.asgi

# To start server without gunicorn
./manage.py runserver

# Lint project
pylint --load-plugins pylint_django app/ settings/

pg_restore -U postgres -f /worldinmovies.db.pgsql
```