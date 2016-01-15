# Frontend

Framework:	Jekyll
Language:	Javascript/Ruby
Dependencies:	Worldinmovies-backend

This is the frontend of the worldinmovies.
It relies on the backend services from worldinmovies-backend, to provide map and user information.

To build and start server on port 4000
```
cd src/main/webapp;
bundle exec jekyll serve;
```

To build docker image
```
mvn clean install -Pdocker
```
