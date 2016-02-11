# Frontend

Framework:	Flask
Language:	Javascript/Python
Dependencies:	Worldinmovies-backend, Gunicorn http://docs.gunicorn.org/en/stable/install.html

This is the frontend of the worldinmovies.
It relies on the backend services from worldinmovies-backend, to provide map and user information.

To build and start server on port 8000
```
cd src/main/webapp;
gunicorn --config=gunicorn.config.py wsgi:app;
```

To build docker image
```
mvn clean install -Pdocker
```
