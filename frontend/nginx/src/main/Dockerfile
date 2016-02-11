FROM tutum/nginx
RUN rm /etc/nginx/sites-enabled/*
ADD sites-enabled/ /etc/nginx/sites-enabled
RUN rm /etc/nginx/nginx.conf
ADD nginx.conf /etc/nginx/nginx.conf
ADD certs/ /etc/nginx/certs

