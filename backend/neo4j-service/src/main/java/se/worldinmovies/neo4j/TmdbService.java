package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class TmdbService {
    private final WebClient webClient;

    public TmdbService(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Flux<T> getData(String uri, Class<T> clazz) {
        try {
            log.info("Fetching");
            return webClient.get().uri(uri)
                    .retrieve()
                    .bodyToFlux(clazz)
                    .log("Found")
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
        } catch (WebClientResponseException e) {
            System.out.println("ResponseException: " + e.getMessage());
            throw e;
        } catch (WebClientRequestException e) {
            System.out.println("RequestException: " + e.getMessage());
            throw e;
        }
    }
}
