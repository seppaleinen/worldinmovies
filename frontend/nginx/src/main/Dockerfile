FROM nginx:1.13.5-alpine

RUN addgroup -g 1000 -S www-data && adduser -u 1000 -D -S -G www-data www-data
ADD sites-enabled/ /etc/nginx/sites-enabled
ADD nginx.conf /etc/nginx/nginx.conf
ADD certs/ /etc/nginx/certs

CMD ["/usr/sbin/nginx"]