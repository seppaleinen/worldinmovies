FROM smebberson/alpine-nginx
RUN rm /etc/nginx/nginx.conf
ADD sites-enabled/ /etc/nginx/sites-enabled
ADD nginx.conf /etc/nginx/nginx.conf
ADD certs/ /etc/nginx/certs
