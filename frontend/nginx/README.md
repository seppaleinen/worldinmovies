# NGINX server

To make it easier to integrate a real ssl-certificate on a server, I've
installed a self-signed certificate in the docker-image, which I exchange
later for the real certificate.

TODO:
A way to automatically update letsencrypt ssl certificate.
```
sudo git clone https://github.com/letsencrypt/letsencrypt \
    /opt/letsencrypt && /opt/letsencrypt/letsencrypt-auto \
     certonly -t --keep --authenticator webroot \
      -w /var/www/cybermoose.org/public_html -d cybermoose.org -d www.cybermoose.org
```

[Local CDN in nGinx](https://jesus.perezpaz.es/2014/02/configure-subdomain-as-cdn-in-nginx-wordpress-w3-total-cache-configurations/)


Renewing letsencrypt
512 mb ram is too small for letsencrypt so first we boost it with a swapfile
then kill the server for letsencrypt to be able to manage the keys
kill the swapfile and start the server again
```
sudo dd if=/dev/zero of=/swapfile bs=1024 count=524288
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

docker-compose stop nginx
/opt/letsencrypt/letsencrypt-auto renew
sudo swapoff /swapfile
docker-compose up -d nginx
```