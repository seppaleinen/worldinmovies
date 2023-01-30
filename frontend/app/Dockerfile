FROM certbot/certbot as certbot
#RUN openssl req -x509 -out /localhost.crt -keyout /localhost.key \
#      -newkey rsa:4096 -nodes -sha256 \
#      -subj '/CN=localhost' -extensions EXT -config <( \
#       printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth") && \
#    cat /localhost.crt /localhost.key > /localhost.crt \
RUN openssl req -x509 -nodes -days 365 -newkey rsa:4096 -sha256 -keyout /localhost.key -out /localhost.crt \
    -subj "/C=SE/ST=QC/O=Company,INC./CN=localhost" && \
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


FROM nginx:alpine as prod

ENV TZ=Europe/Stockholm
RUN addgroup -g 1000 -S www-data ; adduser -u 1000 -D -S -G www-data www-data
# Get selfsigned certificate as fallback

COPY --from=certbot /localhost.crt  /etc/nginx/server.crt
COPY --from=certbot /localhost.key  /etc/nginx/server.key
COPY --from=certbot /dhparam.pem    /etc/nginx/ssl-dhparams.pem
COPY ./nginx/ssl-params.conf        /etc/nginx/ssl-params.conf

COPY ./nginx/sites-enabled/ /etc/nginx/sites-enabled
COPY ./nginx/nginx.conf /etc/nginx/nginx.conf
COPY ./nginx/.htpasswd /etc/nginx/.htpasswd
COPY --from=builder /app/build/ /var/www/app/

#RUN certbot certonly --webroot -w /var/www/certbot \
#        1 \
#        davidbaeriksson@gmail.com \
#        worldinmovies.duckdns.org:81 \
#        --rsa-key-size 4096 \
#        --agree-tos \
#        --force-renewal"
# Test configs
RUN /usr/sbin/nginx -t
CMD ["/usr/sbin/nginx"]
