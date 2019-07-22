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
* Verify memory consumption

* Best 10 of each country
	- additional fields
		- imdb_id
		- year
		- id
	- response as json
	- look into using other than lateral join, as to make tests work
	- alternatively, use postgres as test-db with pytest-pgsql or similar
	- weigh ratings correctly
		- (WR) = (v ÷ (v+m)) × R + (m ÷ (v+m)) × C where:
          R = average for the movie (mean) = (Rating)
          v = number of votes for the movie = (votes)
          m = minimum votes required to be listed in the Top 250 (currently 25000)
          C = the mean vote across the whole report (currently 7.0)
* Convert imdb watched list to world in movies format
	- wanted response-fields
		- countrycode
		- name
		- year
		- imdb_id
		- id
		- personal_rating
		- rating
		- unmatched
			- name
			- year
			- imdbid
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