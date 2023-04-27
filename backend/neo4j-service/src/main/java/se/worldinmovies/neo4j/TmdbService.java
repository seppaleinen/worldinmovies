package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
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

    public TmdbService(@Qualifier("tmdbWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Flux<T> getData(String uri, Class<T> clazz) {
        try {
            return webClient.get().uri(uri)
                    .retrieve()
                    .bodyToFlux(clazz)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
        } catch (WebClientException e) {
            log.error("ResponseException: " + e.getMessage());
            throw e;
        }
    }
}
