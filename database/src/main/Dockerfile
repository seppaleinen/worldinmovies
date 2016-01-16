FROM mongo
ADD resources/countries_base.csv countries_base.csv
RUN bash -c 'touch /countries_base.csv'
#RUN mongod --fork --syslog && mongoimport -d worldinmovies -c CountryEntity --type csv --file countries_base.csv --headerline
EXPOSE 27017
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar", "--spring.data.mongodb.uri=mongodb://mongo/worldinmovies"]
