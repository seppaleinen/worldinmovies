package se.worldinmovies.neo4j;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
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
import se.worldinmovies.neo4j.domain.Movie;
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

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(topics = NewKafkaConsumer.TOPIC, partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9095", "port=9095"})
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
    @Autowired
    private ReactiveNeo4jTemplate template;

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
        Flux.just(movieRepository, genreRepository, countryRepository, languageRepository)
                .flatMap(ReactiveCrudRepository::deleteAll)
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    @AfterEach
    public void teardown() {
        WireMock.reset();
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
        movieRepository.save(new MovieEntity(Movie.builder().movieId(123).build())).block();
        producer.send(NewKafkaConsumer.TOPIC, "DELETE", "123");
        Awaitility.await().untilAsserted(() -> movieRepository.existsById(123).block());
    }

    @Test
    public void canConsumeNEW() {
        stubUrlWithData("/movie/2", "response.json");
        beforeAll();

        producer.send(NewKafkaConsumer.TOPIC, "NEW", "2");

        long before = System.currentTimeMillis();
        try {
            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .until(() -> movieRepository.existsById(2).retry(10).block());

            Optional<MovieEntity> asd = movieRepository.findById(2).blockOptional();
            assertTrue(asd.isPresent());
            assertEquals("", asd.get().getMovieId(), 2);
        } finally {
            long after = System.currentTimeMillis();
            System.out.println("Took: " + (after - before));
        }

        MovieEntity movie = movieRepository.findById(2).blockOptional().orElseThrow(() -> new RuntimeException("asdlkj"));

        assertEquals("", "Ariel", movie.getOriginalTitle());
        assertEquals("", 3, movie.getGenres().size());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Drama")), movie.getGenres().toString());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Comedy")), movie.getGenres().toString());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Crime")), movie.getGenres().toString());

        assertTrue(movie.getProducedBy().stream().anyMatch(a -> Objects.equals(a.getIso(), "FI")), movie.getProducedBy().toString());
        assertTrue(movie.getSpokenLanguages().stream().anyMatch(a -> Objects.equals(a.getIso(), "fi")), movie.getSpokenLanguages().toString());
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/genres"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/langs"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/countries"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/movie/2"));
    }


    @Test
    public void canConsumeMultiples() {
        stubUrlWithData("/movie/2,3", "movie2and3.json");
        beforeAll();

        producer.send(NewKafkaConsumer.TOPIC, "NEW", "2");
        producer.send(NewKafkaConsumer.TOPIC, "NEW", "3");

        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> movieRepository.existsById(2).retry(10).block());

        MovieEntity asd = movieRepository.findById(2).blockOptional().orElseThrow();
        assertEquals("", 2, asd.getMovieId());

        MovieEntity movie = movieRepository.findById(2).blockOptional().orElseThrow(() -> new RuntimeException("asdlkj"));

        assertEquals("", "Ariel", movie.getOriginalTitle());
        assertEquals("", 3, movie.getGenres().size());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Drama")), movie.getGenres().toString());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Comedy")), movie.getGenres().toString());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Crime")), movie.getGenres().toString());

        assertTrue(movie.getProducedBy().stream().anyMatch(a -> Objects.equals(a.getIso(), "FI")), movie.getProducedBy().toString());
        assertTrue(movie.getSpokenLanguages().stream().anyMatch(a -> Objects.equals(a.getIso(), "fi")), movie.getSpokenLanguages().toString());
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/genres"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/langs"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/countries"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/movie/2,3"));

        verifyCount(GenreEntity.class, 19L);
        verifyCount(LanguageEntity.class, 187L);
        verifyCount(CountryEntity.class, 251L);
    }

    @Test
    public void canConsumeAfterEachOther() {
        stubUrlWithData("/movie/2", "response.json");
        stubUrlWithData("/movie/3", "movie3.json");
        beforeAll();

        producer.send(NewKafkaConsumer.TOPIC, "NEW", "2");
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> movieRepository.existsById(2).retry(10).block());

        producer.send(NewKafkaConsumer.TOPIC, "NEW", "3");
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> movieRepository.existsById(3).retry(10).block());

        MovieEntity asd = movieRepository.findById(2).blockOptional().orElseThrow();
        assertEquals("", 2, asd.getMovieId());

        MovieEntity movie = movieRepository.findById(2).blockOptional().orElseThrow(() -> new RuntimeException("asdlkj"));

        assertEquals("", "Ariel", movie.getOriginalTitle());
        assertEquals("", 3, movie.getGenres().size());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Drama")), movie.getGenres().toString());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Comedy")), movie.getGenres().toString());
        assertTrue(movie.getGenres().stream().anyMatch(a -> Objects.equals(a.getName(), "Crime")), movie.getGenres().toString());

        assertTrue(movie.getProducedBy().stream().anyMatch(a -> Objects.equals(a.getIso(), "FI")), movie.getProducedBy().toString());
        assertTrue(movie.getSpokenLanguages().stream().anyMatch(a -> Objects.equals(a.getIso(), "fi")), movie.getSpokenLanguages().toString());
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/genres"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/langs"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/dump/countries"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/movie/2"));
        verify(1, RequestPatternBuilder.newRequestPattern().withUrl("/movie/3"));

        verifyCount(GenreEntity.class, 19L);
        verifyCount(LanguageEntity.class, 187L);
        verifyCount(CountryEntity.class, 251L);
    }

    public void verifyCount(Class<?> clazz, long expectedCount) {
        Long actualCount = template.count(clazz).blockOptional().orElse(null);
        assertEquals(String.format("Result should have been %s but was: %s", expectedCount, actualCount), expectedCount, actualCount);
    }


    @Test
    public void canConsumeEmptyResponse() {
        stubFor(
                WireMock.get(UrlPathPattern.ANY)
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[]"))
        );

        producer.send(NewKafkaConsumer.TOPIC, "NEW", "3");
        Awaitility.await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertFalse(movieRepository.existsById(3).blockOptional().orElse(false)));
    }

    @Test
    public void relationsAreSaved() {
        MovieEntity movie = new MovieEntity(Movie.builder().movieId(1).build());
        List<LanguageEntity> languages = List.of(new LanguageEntity("sv", "svenska", "swedish"));
        CountryEntity country = new CountryEntity("SE", "Sweden", languages);
        movie.getProducedBy().add(country);
        movieRepository.save(movie).block();

        MovieEntity foundMovie = movieRepository.findById(1).blockOptional().orElseThrow(() -> new RuntimeException("No movie"));
        assertNotNull(foundMovie.getProducedBy());
        assertFalse(foundMovie.getProducedBy().isEmpty());
        CountryEntity foundCountry = foundMovie.getProducedBy().stream().findFirst().get();
        assertEquals("", "SE", foundCountry.getIso());

        LanguageEntity language = foundCountry.getLanguages().stream().findFirst().orElseThrow(() -> new RuntimeException("No Language"));
        assertEquals("", "sv", language.getIso());
    }
}
