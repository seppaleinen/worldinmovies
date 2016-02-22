# NGINX server

This is supposed to be my attempt at setting up a server for my webapp

To make it easier to integrate a real ssl-certificate on a server, I've
installed a self-signed certificate in the docker-image, which I exchange
later for the real certificate.

TODO:
A way to automatically update letsencrypt ssl certificate.
```
sudo git clone https://github.com/letsencrypt/letsencrypt \
    /opt/letsencrypt /opt/letsencrypt/letsencrypt-auto \
     certonly -t --keep --authenticator webroot \
      -w /var/www/cybermoose.org/public_html -d cybermoose.org -d www.cybermoose.org
```

Local CDN
https://jesus.perezpaz.es/2014/02/configure-subdomain-as-cdn-in-nginx-wordpress-w3-total-cache-configurations/
