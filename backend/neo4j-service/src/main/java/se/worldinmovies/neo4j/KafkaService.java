package se.worldinmovies.neo4j;

import lombok.extern.java.Log;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log
@Service
public class KafkaService {
    public static final String TOPIC = "data_dump";

    private final Neo4jService neo4jService;

    public KafkaService(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    // default topic=movie, dump=data_dump
    //@KafkaListener(topics = TOPIC, id = "12345")
    public void consume(@Payload List<ConsumerRecord<String, String>> consumerRecords) {
        try {
            Flux.fromIterable(consumerRecords)
                    .collectMultimap(ConsumerRecord::key, record -> Integer.valueOf(record.value()))
                    .blockOptional().orElse(Map.of())
                    .forEach(this::handleEachEventType);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void handleEachEventType(String key, Collection<Integer> value) {
        log.info(String.format("Handling: %s:%s", key, value));
        switch (key) {
            case "NEW", "UPDATE" -> neo4jService.handleNewAndUpdates(value)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(data -> log.info("Processed: " + data),
                            err -> {},
                            () -> System.out.println("Processing completed"));
            case "DELETE" -> neo4jService.delete(value).block();
            default -> System.out.println("Unknown eventType: " + key + ". Ignoring...");
        }
    }
}
