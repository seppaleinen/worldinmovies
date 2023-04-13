package se.worldinmovies.neo4j;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class NewKafkaConsumer {
    private static final String TOPIC = "data_dump";

    private final ReceiverOptions<String, String> receiverOptions;

    private final Neo4jService neo4jService;

    @Autowired
    public NewKafkaConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers, Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        receiverOptions = ReceiverOptions.create(props);
    }

    public Disposable consumeMessages(String topic) {
        ReceiverOptions<String, String> options = receiverOptions.subscription(Collections.singleton(topic))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        Flux<ReceiverRecord<String, String>> kafkaFlux = KafkaReceiver.create(options).receive();
        return kafkaFlux
                .log("Received event")
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(record -> {
                    ReceiverOffset offset = record.receiverOffset();
                    System.out.printf("Received message: key=%s value=%s\n",
                            record.key(),
                            record.value());
                    String key = record.key();
                    Integer value = Integer.valueOf(record.value());
                    switch (key) {
                        case "NEW", "UPDATE" -> {
                            return neo4jService.handleNewAndUpdates(List.of(value))
                                    .log("CONSUMED")
                                    .map(a -> offset);
                        }
                        case "DELETE" -> {
                            return neo4jService.delete(List.of(value))
                                    .flux()
                                    .map(a -> offset);
                        }
                        default -> {
                            return Flux.just(offset);
                        }
                    }
                })
                .log("Waiting")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(ReceiverOffset::acknowledge);
    }

    @PostConstruct
    public void init() {
        consumeMessages(TOPIC);
    }
}
