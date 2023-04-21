package se.worldinmovies.neo4j;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.domain.Country;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.domain.Language;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;

@EnableReactiveNeo4jRepositories
@EnableTransactionManagement
@EnableKafka
@Configuration
public class Config {
    @Bean
    public WebClient webClient(@Value("${tmdb_url}") String tmdbUrl, WebClient.Builder webClientBuilder) {
        final int twoMB = 2 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(twoMB))
                .build();

        return webClientBuilder.baseUrl(tmdbUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean(name = "baseWebClient")
    public WebClient baseWebClient(@Value("${base_url}") String baseUrl, WebClient.Builder webClientBuilder) {
        final int twoMB = 2 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(twoMB))
                .build();

        return webClientBuilder.baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    public ReactiveTransactionManager reactiveTransactionManager(
            Driver driver,
            ReactiveDatabaseSelectionProvider databaseNameProvider) {
        return new ReactiveNeo4jTransactionManager(driver, databaseNameProvider);
    }
}
