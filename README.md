# Worldinmovies

##Todo
* Figure out a way to show results from imdb.csv on map
* Complete the batch regex
* Figure out a way to map movies to countries a faster way
* Import imdb ratings for the movies we've imported


For the moment, ignore all things about users and just focus on making it work "stateless"
1. Load top 5 movies from all countries at startpage
2. When uploaded imdb csv-file then colorcode all countries if seen or not seen


To start docker instances by maven
```
mvn clean install -Pdocker
docker-compose up
```

To start docker by dockerhub
```
docker-compose pull
docker-compose up
```



sudo git clone https://github.com/letsencrypt/letsencrypt \
    /opt/letsencrypt /opt/letsencrypt/letsencrypt-auto \
     certonly -t --keep --authenticator webroot \
      -w /var/www/cybermoose.org/public_html -d cybermoose.org -d www.cybermoose.org
