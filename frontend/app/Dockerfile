FROM node:19-alpine AS builder

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# install and cache app dependencies
COPY package.json /app/
RUN npm install --silent --production

COPY . /app/
RUN npm run build


FROM nginx:alpine

ENV TZ=Europe/Stockholm
RUN addgroup -g 1000 -S www-data ; adduser -u 1000 -D -S -G www-data www-data
COPY --from=builder /app/nginx/sites-enabled/ /etc/nginx/sites-enabled
COPY --from=builder /app/nginx/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/build/ /var/www/app/
COPY ./nginx/.htpasswd /etc/apache2/.htpasswd

# Test configs
RUN /usr/sbin/nginx -t
CMD ["/usr/sbin/nginx"]
