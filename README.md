# World in Movies


This webapp project is for showing which parts of the world that you've seen movies from.
Countries that you've seen a movie from, appears as green.
And countries that you haven't seen yet appears as red.

In future might add functionality to add different graphs


As IMDB no longer exposes the data on their FTP servers, and have removed the country of origin  
data from their new API. I'll try and make a new start of this project from scratch.

I've decided against using java, as it's too resource consuming for the basic servers that I will use.


## TODO

* Graph Database
* Maybe one DB for getting all the data from TMDB, then a cron-job or something
    to process the data into graph-db, or like a pre-sorted list of
    "best movies of every country" so that we don't have to terrorize postgres so much
* Better page layout (welcome, world, country, user, import)
* CSS grid instead of tables and muck
* Check if possible to use graph-db to create aggravated queries like
  Movies from france, from the 90's, with the actor "Gerard Depardieux"
* Paginate "best of each country" list
* Github action for creating docker images
* Fixing jest tests
* Using websockets for the import responses (so that the result will 
  be visible even after restarted browser)
* Estimate how many users can view page before timeouts

## Performance Metrics


```docker stats worldinmovies_webapp_1 worldinmovies_backend_1 worldinmovies_db_1 ```

| CONTAINER ID |           NAME          |  CPU % |    MEM USAGE / LIMIT   |   MEM % |      NET I/O   | BLOCK I/O       | PIDS |
|:------------:|:-----------------------:|:------:|:----------------------:|:-------:|:--------------:|:---------------:|:----:|
| faa56a493bf0 | worldinmovies_webapp_1  |  0.00% | 18.16MiB / 3.732GiB    |  0.48%  | 2.69MB / 654kB | 34.9MB / 0B     | 11   |
| 56cee9abd0ea | worldinmovies_backend_1 |  0.03% | 279.8MiB / 3.732GiB    |  7.32%  | 2.63MB / 494kB | 19.3MB / 0B     | 10   |
| 764ee594592b | worldinmovies_db_1      |  0.00% | 27.04MiB / 3.732GiB    |  0.71%  | 2.71MB / 221kB | 5.89GB / 8.37GB | 16   |

## To start
Dependencies:

* Docker https://docs.docker.com/engine/installation/

```bash
docker compose pull
docker compose up --build
docker compose kill #To stop the running services
docker compose down #To stop and cleanup after running services
```

## Examples
#### Top ranked from USA
![Example of map #1](TopRankedFromUS.png)
#### My top ranked from country
![Example of map #2](MyTopRankedFromSE.png)
#### How it looks after uploading imdb ratings
![Example of map #3](CountriesIveSeenMoviesFrom.png)

