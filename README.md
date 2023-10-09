# World in Movies


This webapp project is for showing which parts of the world that you've seen movies from.
Countries that you've seen a movie from, appears as green.
And countries that you haven't seen yet appears as red.

In future might add functionality to add different graphs


As IMDB no longer exposes the data on their FTP servers, and have removed the country of origin  
data from their new API. I'll try and make a new start of this project from scratch.

I've decided against using java, as it's too resource consuming for the basic servers that I will use.


## TODO

* Fixing jest tests
* Estimate how many users can view page before timeouts

## Performance Metrics



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

