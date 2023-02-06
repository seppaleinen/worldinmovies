FROM certbot/certbot as certbot

# Generate fall back self signed key
RUN openssl genrsa -out /privkey.pem 2048 && \
    openssl req -new -nodes -sha256 -key /privkey.pem -out server.csr -subj "/C=SE/ST=QC/O=Company,INC./CN=localhost" && \
    openssl x509 -req -trustout -days 365 -in server.csr -signkey /privkey.pem -out /fullchain.pem && \
    openssl dhparam -out /dhparam.pem 2048

FROM node:19-alpine AS builder

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# install and cache app dependencies
COPY package.json /app/
COPY tsconfig.json /app/
RUN npm install --silent --production

ADD public/ /app/public/
ADD src/ /app/src/

RUN npm run build


FROM georgjung/nginx-brotli:alpine as prod

ENV TZ=Europe/Stockholm
RUN addgroup -g 1000 -S www-data ; adduser -u 1000 -D -S -G www-data www-data

RUN mkdir -p /etc/letsencrypt/live/worldinmovies.duckdns.org

COPY --from=certbot /fullchain.pem  /etc/letsencrypt/live/worldinmovies.duckdns.org/fullchain.pem
COPY --from=certbot /privkey.pem    /etc/letsencrypt/live/worldinmovies.duckdns.org/privkey.pem
COPY --from=certbot /dhparam.pem    /etc/nginx/ssl-dhparams.pem

# Replace self-signed certs with real ones if existing
COPY nginx/certs/live/worldinmovies.duckdns.org/fakefile \
    nginx/certs/live/worldinmovies.duckdns.org/fullchain.pe? \
    nginx/certs/live/worldinmovies.duckdns.org/privkey.pe? \
    /etc/letsencrypt/live/worldinmovies.duckdns.org/

COPY --from=builder /app/build/     /var/www/app/

COPY ./nginx/ssl-params.conf        /etc/nginx/ssl-params.conf
COPY ./nginx/sites-enabled/         /etc/nginx/sites-enabled
COPY ./nginx/nginx.conf             /etc/nginx/nginx.conf
COPY ./nginx/.htpasswd              /etc/nginx/.htpasswd

# Test configs
RUN /usr/sbin/nginx -t

CMD ["/usr/sbin/nginx"]
