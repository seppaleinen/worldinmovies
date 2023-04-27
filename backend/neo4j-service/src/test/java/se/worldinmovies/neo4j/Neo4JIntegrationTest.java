package se.worldinmovies.neo4j;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.worldinmovies.neo4j.entity.*;
import se.worldinmovies.neo4j.repository.MovieRepository;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(topics = Neo4JIntegrationTest.testtopic, partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9095", "port=9095"})
@WireMockTest(httpPort = 9999)
public class Neo4JIntegrationTest {
    static final String testtopic = "supertopic";
    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5")
            .withStartupTimeout(Duration.ofMinutes(5));
    @Autowired
    private KafkaTemplate<String, String> producer;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReactiveNeo4jTemplate neo4jTemplate;
    @Autowired
    private Neo4jService neo4jService;


    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.topic", () -> Neo4JIntegrationTest.testtopic);
        registry.add("tmdb_url", () -> "http://localhost:9999");
        registry.add("base_url", () -> "http://localhost:9999");
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @BeforeEach
    public void setup() {
        stubUrlWithData(null, "votes.json");
        Flux.just(MovieEntity.class, GenreRelations.class, LanguageRelations.class, CountryRelations.class)
                .flatMap(e -> neo4jTemplate.deleteAll(e))
                .collectList()
                .retry(5)
                .block();
    }

    @AfterEach
    public void teardown() {
        WireMock.reset();
    }

    @Test
    public void canConsumeDELETE() {
        movieRepository.save(new MovieEntity(123)).block();

        Awaitility.await().until(() -> movieRepository.existsById(123).block());

        producer.send(Neo4JIntegrationTest.testtopic, "DELETE", "123");
        Awaitility.await().until(() -> !movieRepository.existsById(123).blockOptional().orElseThrow());
    }

    @Test
    public void canConsumeNEW() {
        int id = 2;
        stubUrlWithData("/movie/" + id, "response.json");
        stubUrlWithData("/votes/" + id, "votes.json");

        producer.send(Neo4JIntegrationTest.testtopic, "NEW", String.valueOf(id));

        MovieEntity movie = verifyMovie(id, "Ariel");
        assertFalse(movie.getGenres().isEmpty(), "There should be genres connected to movie");
        assertFalse(movie.getProducedBy().isEmpty(), "There should be countries connected to movie");
        assertFalse(movie.getSpokenLanguages().isEmpty(), "There should be languages connected to movie");
        assertEquals(19, neo4jTemplate.count(GenreEntity.class).block());
        assertEquals(187, neo4jTemplate.count(LanguageEntity.class).block());
        assertEquals(251, neo4jTemplate.count(CountryEntity.class).block());
    }

    @Test
    public void canConsumeNEW2() {
        int id = 644553;
        stubUrlWithData(null, "largeresponse.json");

        Flux.just(644553, 644555, 644571, 644584, 644586, 644591, 644601, 644613, 644614, 644625, 644636, 644650, 644652, 644670, 644709, 644712, 644754, 644756, 644761, 644765, 644775, 644784, 644822, 644828, 644831)
                .map(String::valueOf)
                .map(a -> producer.send(Neo4JIntegrationTest.testtopic, "NEW", a))
                .subscribe();

        verifyMovie(id, "Glasba je časovna umetnost 3: LP film Laibach");
        verifyMovie(644555, "Vroom!-Vroom!");
        verifyMovie(644571, "Meidän poikamme merellä");
        verifyMovie(644831, "Blithe Spirit");
    }

    @Test
    public void canConsumeManyMany() {
        List<Integer> ids = List.of(644553, 644555, 644571, 644584, 644586, 644591, 644601, 644613, 644614, 644625, 644636, 644650, 644652, 644670, 644709, 644712, 644754, 644756, 644761, 644765, 644775, 644784, 644822, 644828, 644831);
        stubUrlWithData("/movie/" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")), "largeresponse.json");
        List<Integer> ids2 = List.of(758911, 758922, 758924, 758925, 758929, 758934, 758941, 758948, 758952, 758956, 758982, 758984, 758992, 759012, 759024, 759045, 759051, 759054, 759073, 759087, 759097, 759101, 759109, 795105, 813795);
        stubUrlWithData("/movie/" + ids2.stream().map(String::valueOf).collect(Collectors.joining(",")), "largeresponse2.json");

        List<Integer> allIds = new ArrayList<>(ids);
        allIds.addAll(ids2);
        neo4jService.handleNewAndUpdates(ids)
                .collectList()
                .blockOptional().orElseThrow()
                .forEach(id -> verifyMovie(id, null));

        neo4jService.handleNewAndUpdates(ids2)
                .collectList()
                .blockOptional().orElseThrow()
                .forEach(id -> verifyMovie(id, null));

        allIds.forEach(id -> verifyMovie(id, null));
        assertEquals(19L, neo4jTemplate.findAll(GenreEntity.class).count().block());
    }

    @Test
    public void as() {
        List<Integer> ids = List.of(19995, 19996, 19997, 19998, 20002, 20006, 20009, 20012, 20013, 20015, 20017, 20018, 20019, 20021, 20024, 20029, 20030, 20031, 20032, 20033, 20044, 20045, 20048, 20121, 20941);
        stubUrlWithData("/movie/" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")), "largeresponse3.json");

        Flux.fromIterable(ids)
                .bufferTimeout(25, Duration.ofMillis(10))
                .flatMap(id -> neo4jService.handleNewAndUpdates(id))
                .collectList()
                .block(Duration.ofSeconds(10));

        ids.forEach(id -> verifyMovie(id, null));

        assertEquals(19L, neo4jTemplate.findAll(GenreEntity.class).count().block());
    }

    @Test
    public void testPersistingTwice() {
        int movieId = 2;
        stubUrlWithData("/movie/" + movieId, "response.json");

        neo4jService.handleNewAndUpdates(List.of(movieId))
                .as(StepVerifier::create)
                .expectNext(movieId)
                .expectComplete()
                .verify();

        verifyMovie(movieId, "Ariel");

        stubUrlWithData("/movie/" + movieId, "response_update.json");

        neo4jService.handleNewAndUpdates(List.of(movieId))
                .as(StepVerifier::create)
                .expectNext(movieId)
                .expectComplete()
                .verify();

        verifyMovie(movieId, "Brother's War");
    }

    private MovieEntity verifyMovie(int id, String optionalMovieTitle) {
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> Mono.defer(() -> movieRepository.existsById(id)).block());

        MovieEntity movie = movieRepository.findById(id).blockOptional()
                .orElseGet(() -> fail(String.format("Movie: %s must be present", id)));

        Optional.ofNullable(optionalMovieTitle)
                .ifPresent(expectedMovieTitle -> assertEquals(expectedMovieTitle, movie.getOriginalTitle()));

        assertEquals(id, movie.getMovieId());

        return movie;
    }

    private static void stubUrlWithData(String path, String file) {
        URL resourceFile = Neo4JIntegrationTest.class.getClassLoader().getResource(file);
        try {
            String data = Files.readString(Paths.get(resourceFile.toURI()));
            stubFor(
                    WireMock.get(path == null ? UrlPathPattern.ANY : WireMock.urlPathMatching(path))
                            .willReturn(WireMock.aResponse()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                    .withBody(data))
            );
        } catch (Exception e) {
            fail("Nono" + e.getMessage());
        }
    }
}
