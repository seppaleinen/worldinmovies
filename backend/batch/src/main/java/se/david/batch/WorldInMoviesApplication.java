package se.david.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class WorldInMoviesApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorldInMoviesApplication.class, args);
    }
}
