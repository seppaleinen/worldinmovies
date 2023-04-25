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

@Service
@Slf4j
public class ImdbService {
    private final WebClient baseWebClient;

    public ImdbService(@Qualifier("baseWebClient") WebClient baseWebClient) {
        this.baseWebClient = baseWebClient;
    }

    public <T> Flux<T> getData(String uri, Class<T> clazz) {
        return baseWebClient.get().uri(uri)
                .retrieve()
                .bodyToFlux(clazz)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
    }
}
