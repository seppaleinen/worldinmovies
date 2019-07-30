# Frontend of World in Movies

This is the actual frontend of the project.
Here there will be different ways to look at data regarding movies, hopefully.

First hand, it's supposed to show the top-ranked movies from each country.
Second hand, show you which countries you've seen movies from and from which you haven't.



### TODO

* Check out serving from nginx instead of serve
  - docker multi-stage build
    1. node -> npm build
    2. nginx fetch build/
* Sentry
* Tests
* Check if possible to minify, or make site load faster
* Pages
  - Admin
    - Start imports
  - First page
  - "Visited" Countries


### Commands

```bash
# Start server on :3000
npm start

# Build and serve on :5000
npm run build && serve -s build

npm test
```