# Neo4J Service


http://localhost:7474/browser/

```bash
jenv local corretto64-17.0.6

mvn -s settings.xml clean install

java -jar -Dspring.profiles.active=local target/neo4j-service-0.0.1-SNAPSHOT.jar

docker exec -ti worldinmovies_kafka_1 /opt/bitnami/kafka/bin/kafka-topics.sh --delete --topic movie --bootstrap-server localhost:9092
```
