FROM nginx:alpine
RUN addgroup -g 1000 -S www-data ; adduser -u 1000 -D -S -G www-data www-data
COPY nginx.conf /etc/nginx/nginx.conf

# Test configs
RUN /usr/sbin/nginx -t
CMD ["/usr/sbin/nginx"]
