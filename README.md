# Worldinmovies


This webapp project is for showing which parts of the world that you've seen movies from.
Countries that you've seen a movie from, appears as green.
And countries that you haven't seen yet appears as red.

For now the only way to add which movies that you've seen is to upload the 
"rated movies" export file from imdb

Prototype version is up at https://worldinmovies.duckdns.org/

##Todo
* Create admin page
* Move batch jobs to functions from admin page
* Create signup/login functionality
* Connect movies to user
* Load "seen movies" from database instead of html-tag
* Ability to add "seen movie" from page
  * Search field
  * Add button from country popup-page


##To start docker instances by maven:
Dependencies:

* Maven https://maven.apache.org/
* Docker https://docs.docker.com/engine/installation/
* Docker-compose https://docs.docker.com/compose/install/
* MongoDB https://docs.mongodb.org/manual/administration/install-community/ # For maven tests only.. mvn clean install -Dmaven.test.skip=True to skip
```
mvn clean install -Pdocker
docker-compose up --build
```

##To start docker by dockerhub
Dependencies:

* Docker https://docs.docker.com/engine/installation/
* Docker-compose https://docs.docker.com/compose/install/
```
docker-compose pull
docker-compose up --build
docker-compose kill #To stop the running services
docker-compose down #To stop and cleanup after running services
```

## General design
![Architecture](worldinmovies-architecture.png)

