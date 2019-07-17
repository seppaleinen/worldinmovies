# Backend

This part will be the backend handling imports and serving data to the front-end.

In future, this might be separated into different backend systems but for now will be one big chunk.


Basically will be handling
* Import
  - Daily file export from TMDB with all movie ids that are available
  - Fetching movie data from TMDB, dependent on the movie ids received from the daily file
* Serving Frontend

### Todo

* Gunicorn or similar server
* ETA of file download
* Show prints in docker logs
* Cron-like way of starting imports daily
* Move import apis behind /admin
* If import fails, save to separate failure-table with movie-id, exception message, and raw dump
* API for checking how import is going
* Verify memory consumption

### Requirements

* Python3
* Postgresql


```bash
# Install requirements
pip3 install -r requirements

# To create and update database
./manage.py makemigrations && ./manage.py migrate

# To start server
./manage.py runserver


# Or with docker
# Build and start servers
docker-compose up --build -d

# Shut down servers
docker-compose kill

# Shut down servers and delete all data
docker-compose down
```