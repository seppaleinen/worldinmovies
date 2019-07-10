# Import

This part will be responsible for importing all movie data  
and saving to database. 

Rate limit is 40 requests every 10 seconds  
response header X-RateLimit will describe how many requests are possible  
before being limited. 
When limited 429 will be returned with a response header Retry-After  
describing how many seconds to wait until next request is possible.

# Todo

* https://pretenders.readthedocs.io/en/latest/
* Get file http://files.tmdb.org/p/exports/movie_ids_<<date>>_<<month>_<<year>>.json.gz
* endpoint/3/movie/{movie_id}/lists?api_key=<<api_key>>

* https://developers.themoviedb.org/3/getting-started/request-rate-limiting
* https://www.themoviedb.org/settings/api


```bash
# Install requirements
pip install -r requirements

# To start importer
python3 importer.py

# Run tests
behave
```