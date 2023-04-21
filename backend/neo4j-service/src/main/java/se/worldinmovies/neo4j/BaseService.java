package se.worldinmovies.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class BaseService {
    private final WebClient baseWebClient;

    public BaseService(WebClient baseWebClient) {
        this.baseWebClient = baseWebClient;
    }

    public <T> Flux<T> getData(String uri, Class<T> clazz) {
        try {
            return baseWebClient.get().uri(uri)
                    .retrieve()
                    .bodyToFlux(clazz)
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
