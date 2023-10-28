# Frontend of World in Movies

This is the actual frontend of the project.
Here there will be different ways to look at data regarding movies, hopefully.

First hand, it's supposed to show the top-ranked movies from each country.
Second hand, show you which countries you've seen movies from and from which you haven't.


### Commands

```bash
# Start server on :3000
REACT_APP_IMDB_URL=http://localhost:8000 REACT_APP_TMDB_URL=http://localhost:8020 REACT_APP_NEO_URL=http://localhost:8082 npm start
npm run start

# Build and serve on :5000
npm run build && serve -s build

npm run test
```