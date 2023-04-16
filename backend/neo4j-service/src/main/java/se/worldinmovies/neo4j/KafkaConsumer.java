package se.worldinmovies.neo4j;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import se.worldinmovies.neo4j.entity.MovieEntity;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class KafkaConsumer {
    public static final String TOPIC = "data_dump";

    private final ReceiverOptions<String, String> receiverOptions;
    private final Neo4jService neo4jService;

    @Autowired
    public KafkaConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers, Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-id");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        receiverOptions = ReceiverOptions.create(props);
    }

    public Flux<ReceiverOffset> consumeMessages(String topic) {
        ReceiverOptions<String, String> options = receiverOptions.subscription(Collections.singleton(topic));
        Flux<ReceiverRecord<String, String>> kafkaFlux = KafkaReceiver.create(options).receive();
        return kafkaFlux
                .bufferTimeout(25, Duration.ofMillis(100))
                .onBackpressureBuffer()
                .flatMap(record -> Flux.fromStream(record.stream()
                                .collect(Collectors.groupingBy(ReceiverRecord::key))
                                .entrySet()
                                .stream())
                        .flatMap(this::handleGroupAndReturnOffsets));

    }

    private Flux<ReceiverOffset> handleGroupAndReturnOffsets(Map.Entry<String, List<ReceiverRecord<String, String>>> entry) {
        String key = entry.getKey();
        List<Integer> values = entry.getValue()
                .stream()
                .map(ConsumerRecord::value)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        switch (key) {
            case "NEW", "UPDATE" -> {
                return neo4jService.handleNewAndUpdates(values)
                        .flatMap(a -> Flux.just(a.getMovieId())
                        .mapNotNull(id -> entry.getValue().stream()
                                .filter(b -> id.equals(Integer.valueOf(b.value())))
                                .findAny()
                                .map(ReceiverRecord::receiverOffset)
                                .orElse(null)));
            }
            case "DELETE" -> {
                return neo4jService.delete(values)
                        .then()
                        .flux()
                        .flatMap(a -> Flux.fromStream(entry.getValue().stream()
                                .map(ReceiverRecord::receiverOffset)));
            }
            default -> {
                return Flux.fromIterable(entry.getValue())
                        .map(ReceiverRecord::receiverOffset);
            }
        }
    }

    @PostConstruct
    public void init() {
        consumeMessages(TOPIC)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(ReceiverOffset::acknowledge);
    }
}
