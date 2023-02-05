FROM certbot/certbot

RUN pip install certbot_dns_duckdns

COPY ./duckdns.ini /duckdns.ini

RUN certbot certonly \
      --non-interactive \
      --agree-tos \
      --email davidbaeriksson@gmail.com \
      --preferred-challenges dns \
      --authenticator dns-duckdns \
      --dns-duckdns-credentials /duckdns.ini \
      --dns-duckdns-propagation-seconds 60 \
      -d "worldinmovies.duckdns.org"
