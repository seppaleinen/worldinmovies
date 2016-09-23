# Worldinmovies


This webapp project is for showing which parts of the world that you've seen movies from.
Countries that you've seen a movie from, appears as green.
And countries that you haven't seen yet appears as red.

For now the only way to add which movies that you've seen is to upload the 
"rated movies" export file from imdb

Prototype version is up at https://worldinmovies.duckdns.org/

##Todo
* Figure out a way to show results from imdb.csv on map
* Complete the batch regex
* Figure out a way to map movies to countries a faster way
* Import imdb ratings for the movies we've imported
* New batch job for imdb ratings
* Create admin page
* Move batch jobs to functions from admin page


For the moment, ignoring all things about users and just focus on making it work "stateless"
1. Load top 5 movies from all countries at startpage
2. When uploaded imdb csv-file then colorcode all countries if seen or not seen


##To start docker instances by maven:
Dependencies:

* Maven https://maven.apache.org/
* Docker https://docs.docker.com/engine/installation/
* Docker-compose https://docs.docker.com/compose/install/
* MongoDB https://docs.mongodb.org/manual/administration/install-community/ # For maven tests only.. mvn clean install -Dmaven.test.skip=True to skip
```
mvn clean install -Pdocker
docker-compose up
```

##To start docker by dockerhub
Dependencies:

* Docker https://docs.docker.com/engine/installation/
* Docker-compose https://docs.docker.com/compose/install/
```
docker-compose pull
docker-compose build
docker-compose up
docker-compose kill
```


## General design
![Architecture](worldinmovies-architecture.png)

