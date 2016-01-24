package se.david.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories()
public class WorldInMoviesApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorldInMoviesApplication.class, args);
    }
}
