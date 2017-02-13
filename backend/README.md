# Backend

Language:	Java
Build:		Maven
Framework:	Spring-boot
Database:	MongoDB

This is to be the backend for the worldinmovies webapp.
It will be exposing a REST-API to manage user and map-data on port 10080.

To build and test
```
mvn clean install
```

To build docker image
```
mvn clean install -Pdocker
```
