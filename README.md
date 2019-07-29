# World in Movies


This webapp project is for showing which parts of the world that you've seen movies from.
Countries that you've seen a movie from, appears as green.
And countries that you haven't seen yet appears as red.

In future might add functionality to add different graphs


As IMDB no longer exposes the data on their FTP servers, and have removed the country of origin  
data from their new API. I'll try and make a new start of this project from scratch.

I've decided against using java, as it's too resource consuming for the basic servers that I will use.


## TODO

* Traefik

## To start
Dependencies:

* Docker https://docs.docker.com/engine/installation/
* Docker-compose https://docs.docker.com/compose/install/

```bash
docker-compose pull
docker-compose up --build
docker-compose kill #To stop the running services
docker-compose down #To stop and cleanup after running services
```

## General design
![Architecture](worldinmovies-architecture.png)

