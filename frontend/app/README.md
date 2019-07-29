# Frontend of World in Movies


### TODO

* Check out serving from nginx instead of serve
  - docker multi-stage build
    1. node -> npm build
    2. nginx fetch build/

### Commands

```bash
# Start server on :3000
npm start

# Build and serve on :5000
npm run build && serve -s build

npm test
```