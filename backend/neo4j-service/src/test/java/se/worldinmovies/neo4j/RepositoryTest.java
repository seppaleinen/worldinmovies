package se.worldinmovies.neo4j;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.MovieRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static se.worldinmovies.neo4j.Neo4JIntegrationTest.stubUrlWithData;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(topics = KafkaConsumer.TOPIC, partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9095", "port=9095"})
@WireMockTest(httpPort = 9999)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Slf4j
@DirtiesContext
public class RepositoryTest {
    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5")
            .withStartupTimeout(Duration.ofMinutes(5));
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReactiveNeo4jTemplate template;

    @Autowired
    private Neo4jService neo4jService;

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("tmdb_url", () -> "http://localhost:9999");
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @BeforeAll
    static void beforeAll() {
        stubUrlWithData("/dump/genres", "genres.json");
        stubUrlWithData("/dump/langs", "languages.json");
        stubUrlWithData("/dump/countries", "countries.json");
    }

    @BeforeEach
    void before() {
        Flux.just(MovieEntity.class, GenreEntity.class, CountryEntity.class, LanguageEntity.class)
                .flatMap(a -> template.deleteAll(a))
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    @Test
    public void testSetup() {
        stubUrlWithData("/dump/genres", "genres.json");
        stubUrlWithData("/dump/countries", "countries.json");
        stubUrlWithData("/dump/langs", "languages.json");

        neo4jService.setup();

        assertEquals("", 19, neo4jService.getGenres().block().size());
        assertEquals("", 187, neo4jService.getLanguages().block().size());
        assertEquals("", 251, neo4jService.getCountries().block().size());
    }

    @Test
    public void verifyCaching() {
        stubUrlWithData("/dump/genres", "genres.json");
        stubUrlWithData("/dump/countries", "countries.json");
        stubUrlWithData("/dump/langs", "languages.json");


        neo4jService.setup();
        neo4jService.setup();
        Map<Integer, GenreEntity> genres = neo4jService.getGenres().blockOptional().orElseThrow();
        Map<String, CountryEntity> countries = neo4jService.getCountries().blockOptional().orElseThrow();
        Map<String, LanguageEntity> languages = neo4jService.getLanguages().blockOptional().orElseThrow();

        assertEquals("", 19, genres.size());
        assertEquals("", 187, languages.size());
        assertEquals("", 251, countries.size());

        WireMock.verify(0, RequestPatternBuilder.newRequestPattern().withUrl("/dump/genres"));
        WireMock.verify(0, RequestPatternBuilder.newRequestPattern().withUrl("/dump/langs"));
        WireMock.verify(0, RequestPatternBuilder.newRequestPattern().withUrl("/dump/countries"));
    }

    public void verify(Class<?> clazz, long expectedCount) {
        Long actualCount = template.count(clazz).blockOptional().orElse(null);
        assertEquals(String.format("Result should have been %s but was: %s", expectedCount, actualCount), expectedCount, actualCount);
    }
}
