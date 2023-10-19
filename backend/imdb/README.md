# IMDB Import

This is the part handling data regarding IMDB
Primarily it will try to fetch data from IMDB when a new movie gets fetched from TMDB
and the data-points it collects are alternative titles, and votes

It also handles user-rating imports.


### Notes
* To fetch images, prefix with: https://image.tmdb.org/t/p/w500/

### DB Migration Guide
```bash
docker exec -ti postgres pg_dump -U postgres postgres --clean --file=/tmp/dbexport.pgsql

docker cp postgres:/tmp/dbexport.pgsql .

rsync --progress dbexport.pgsql <ssh-server>:<path>

ssh <ssh-server>

docker cp dbexport.pgsql postgres:/

docker exec -ti postgres psql -d postgres -U postgres -f /dbexport.pgsql
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