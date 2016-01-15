# Worldinmovies

##Todo
* Check out frameworks for interactive world maps	(https://github.com/manifestinteractive/jqvmap)
* Figure out a way to host site and database		-
* Decide on database					(MongoDB)
* Decide on web-framework				(Jekyll - ?)
* Design database structure				-

If the user is not logged in, the default page should show the top rated movies from each country

When a user logs in, the user should be able to vote on a movie and add to the public map
And a user should be able to create their own map with movies that they've watched.

I want the backend and frontend to live separately from each other live microservices.
We'll see how that goes...
But for now just to simplify the docker build I've connected all by maven.

To start docker instances by maven
```
mvn clean install -Pdocker
docker-compose up
```


