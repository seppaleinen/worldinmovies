# NGINX server

To make it easier to integrate a real ssl-certificate on a server, I've
installed a self-signed certificate in the docker-image, which I exchange
later for the real certificate.


[Local CDN in nGinx](https://jesus.perezpaz.es/2014/02/configure-subdomain-as-cdn-in-nginx-wordpress-w3-total-cache-configurations/)


```
git clone https://github.com/letsencrypt/letsencrypt
mkdir /tmp/letsencrypt-auto
letsencrypt/letsencrypt-auto certonly --server https://acme-v01.api.letsencrypt.org/directory -a webroot --webroot-path=/tmp/letsencrypt-auto --agree-dev-preview -d worldinmovies.duckdns.org -d www.worldinmovies.duckdns.org
cd krog-rouletten
```

```
docker-compose stop
letsencrypt/letsencrypt-auto --renew-by-default certonly --server https://acme-v01.api.letsencrypt.org/directory -a webroot --webroot-path=/tmp/letsencrypt-auto --agree-dev-preview -d worldinmovies.duckdns.org -d www.worldinmovies.duckdns.org
cd krog-rouletten
docker-compose up nginx -d
```