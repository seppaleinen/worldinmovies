# Backend

This part will be the backend handling imports and serving data to the front-end.

In future, this might be separated into different backend systems but for now will be one big chunk.


Basically will be handling
* Import
  - Daily file export from TMDB with all movie ids that are available
  - Fetching movie data from TMDB, dependent on the movie ids received from the daily file
* Serving Frontend

### Todo

* Change ETA handling to something that doesnt require interactive shell
* ETA of file download
* Cron-like way of starting imports daily
* Move import apis behind /admin
* If import fails, save to separate failure-table with movie-id, exception message, and raw dump
* Imdb ratings doesn't get all rows
    - e.g id=337401	title=Mulan	fetched=true imdb_id=tt4566758
        * not released yet, therefore has no ratings on imdb
    - e.g id=441595
        * imdb_id is empty
    - e.g id=443005
        * no votes in imdb
    - e.g id=418548 title="everybody happy" year=2016 imdb_id=tt6038926
        * imdb_id should be tt4440508
* Best 10 of each country
	- additional fields
		- imdb_id
		- year
		- id
	- look into using other than lateral join, as to make tests work
	- alternatively, use postgres as test-db with pytest-pgsql or similar
* Search by name
	- wanted response-fields
		- imdb_id
		- id
		- name
		- year
		- poster_url
		- countryname


### Notes
* To fetch images, prefix with: https://image.tmdb.org/t/p/w500/

* DB Migration Guide
  1. ```bash docker exec -ti containerId /bin/sh ```
  2. ```bash pg_dump -U username dbname > dbexport.pgsql ```
  3. logout and ```bash docker copy containerId:/dbexport.pgsql ./dbexport.pgsql ```
  4. clean target db 
    - ```sql
        DROP SCHEMA public CASCADE;
        CREATE SCHEMA public;
        GRANT ALL ON SCHEMA public TO postgres;
        GRANT ALL ON SCHEMA public TO public;
        ```
  5. ```bash docker copy dbexport.pgsql containerId:/dbexport.pgsql ```
  6. ```bash docker exec -ti containerId /bin/sh ```
  7. ```bash psql -U username dbname < dbexport.pgsql ```



### Requirements

* Python3
* Postgresql


```bash
# Install requirements
pip3 install -r requirements

# To create and update database
./manage.py makemigrations && ./manage.py migrate

# To start server with gunicorn
gunicorn --config=gunicorn.config.py settings.wsgi

# To start server without gunicorn
./manage.py runserver

# Lint project
pylint --load-plugins pylint_django app/ settings/

# Or with docker
# Build and start servers
docker-compose up --build -d

# Read logs
docker-compose logs -f

# Shut down servers
docker-compose kill

# Shut down servers and delete all data
docker-compose down
```