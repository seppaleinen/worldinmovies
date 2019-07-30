FROM node:12-alpine

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# install and cache app dependencies
COPY package.json /app/
RUN npm install --silent --production

COPY . /app/
RUN npm run build

# start app
CMD ["serve", "-s", "build"]
