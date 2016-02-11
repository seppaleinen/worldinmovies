# Batch

Language:	Java
Build:		Maven
Framework:	Spring-boot
Database:	MongoDB

This is the part of the system that manages imports of external data.

* A list of countries that the webapp can check
* A list of movies and what country they're from


To build and test
```
mvn clean install
java -jar target/batch.jar
```

To build docker image
```
mvn clean install -Pdocker
```
