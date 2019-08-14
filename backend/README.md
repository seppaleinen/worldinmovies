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
* https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-lcid/70feba9f-294e-491e-b6eb-56532684c37f
* Get movie changes
    - Get list of which movies have been changed between dates
    - https://api.themoviedb.org/3/movie/changes?api_key=<<api_key>>&end_date=2019-08-01&start_date=2019-08-05&page=1
* Get countries
    - Hoped that it would contain languages per country as well. but no.
    - https://api.themoviedb.org/3/configuration/countries?api_key=<<api_key>>
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
  1. ```bash docker exec -ti worldinmovies_db_1 pg_dump -U postgres postgres --clean --file=/tmp/dbexport.pgsql ```
  2. Move postgres-data/dbexport.pgsql to machine where it should be imported
  3. ```bash docker exec -ti worldinmovies_db_1 psql -U postgres --file=/tmp/dbexport.pgsql ```



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

```