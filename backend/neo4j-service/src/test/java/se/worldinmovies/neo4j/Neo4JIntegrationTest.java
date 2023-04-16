package se.worldinmovies.neo4j;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import se.worldinmovies.neo4j.entity.CountryEntity;
import se.worldinmovies.neo4j.entity.GenreEntity;
import se.worldinmovies.neo4j.entity.LanguageEntity;
import se.worldinmovies.neo4j.entity.MovieEntity;
import se.worldinmovies.neo4j.repository.CountryRepository;
import se.worldinmovies.neo4j.repository.GenreRepository;
import se.worldinmovies.neo4j.repository.LanguageRepository;
import se.worldinmovies.neo4j.repository.MovieRepository;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(topics = KafkaConsumer.TOPIC, partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9095", "port=9095"})
@WireMockTest(httpPort = 9999)
public class Neo4JIntegrationTest {
    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5")
            .withStartupTimeout(Duration.ofMinutes(5));
    @Autowired
    private KafkaTemplate<String, String> producer;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private CountryRepository countryRepository;

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
    public void setup() {
        WireMock.reset();
        Flux.just(movieRepository, genreRepository, countryRepository, languageRepository)
                .flatMap(ReactiveCrudRepository::deleteAll)
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    static void stubUrlWithData(String path, String file) {
        URL responseUrl = Neo4JIntegrationTest.class.getClassLoader().getResource(file);
        try {
            String data = Files.readString(Paths.get(responseUrl.toURI()));
            stubFor(
                    WireMock.get(WireMock.urlPathMatching(path))
                            .willReturn(WireMock.aResponse()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                    .withBody(data))
            );
        } catch (Exception e) {
            fail("Nono");
        }
    }

    @Test
    public void canConsumeDELETE() {
        movieRepository.save(new MovieEntity(123)).block();
        producer.send(KafkaConsumer.TOPIC, "DELETE", "123");
        Awaitility.await().untilAsserted(() -> movieRepository.existsById(123).block());
    }

    @Test
    public void canConsumeNEW() {
        stubUrlWithData("/movie/2", "response.json");
        beforeAll();

        producer.send(KafkaConsumer.TOPIC, "NEW", "2");

        long before = System.currentTimeMillis();
        try {
            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .until(() -> movieRepository.existsById(2).retry(10).block());

            Optional<MovieEntity> asd = movieRepository.findById(2).blockOptional();
            assertTrue(asd.isPresent());
            assertEquals(2, asd.get().getMovieId());
        } finally {
            long after = System.currentTimeMillis();
            System.out.println("Took: " + (after - before));
        }

        MovieEntity movie = movieRepository.findById(2).blockOptional().orElseThrow(() -> new RuntimeException("asdlkj"));

        assertEquals("Ariel", movie.getOriginalTitle());
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/genres"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/langs"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/countries"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/movie/2"));
        assertEquals(genreRepository.count().blockOptional().orElseThrow(), 19L);
    }

    @Test
    public void canConsumeNEW2() {
        stubUrlWithData("/movie/2", "largeresponse.json");
        beforeAll();

        List<Integer> ids = IntStream.of(2)
                .boxed()
                .toList();

        Flux.fromIterable(ids)
                .flatMap(a -> Flux.just(producer.send(KafkaConsumer.TOPIC, "NEW", String.valueOf(a))))
                .subscribeOn(Schedulers.parallel())
                .subscribe();

        int id = 644553;
        long before = System.currentTimeMillis();
        try {
            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .until(() -> movieRepository.existsById(id).retry(10).blockOptional().orElse(false));

            Optional<MovieEntity> asd = movieRepository.findById(id).blockOptional();
            assertTrue(asd.isPresent());
            assertEquals(id, asd.get().getMovieId());
        } finally {
            long after = System.currentTimeMillis();
            System.out.println("Took: " + (after - before));
        }

        MovieEntity movie = movieRepository.findById(id).blockOptional().orElseThrow(() -> new RuntimeException("asdlkj"));

        assertEquals(25L, movieRepository.count().block());
        assertEquals("Glasba je časovna umetnost 3: LP film Laibach", movie.getOriginalTitle());
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/genres"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/langs"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/countries"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/movie/2"));
        assertEquals(genreRepository.count().blockOptional().orElseThrow(), 19L);
    }

    @Test
    public void canConsumeEmptyResponse() {
        stubFor(
                WireMock.get(UrlPathPattern.ANY)
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[]"))
        );

        producer.send(KafkaConsumer.TOPIC, "NEW", "3");
        Awaitility.await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertFalse(movieRepository.existsById(3).blockOptional().orElse(false)));
    }

}
