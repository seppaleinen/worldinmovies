package se.worldinmovies.neo4j.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import se.worldinmovies.neo4j.entity.MovieEntity;

import java.util.List;

@Repository
public interface MovieRepository extends ReactiveNeo4jRepository<MovieEntity, Integer> {
    @Query(value = "MATCH (g:Genre)-[gr]->(m:Movie)<-[lr:original_language]-(l:Language) " +
            "WHERE l.iso in ($languageCode) " +
            "RETURN DISTINCT * " +
            "ORDER BY m.weight DESC " +
            "SKIP $skip LIMIT $limit")
    Flux<MovieEntity> findBestByLanguage(List<String> languageCode, int skip, int limit);

    @Query(value = "MATCH (l:Language)-[lr]->(m:Movie)-[cr:produced_by]->(c:Country) " +
            "WHERE c.iso = $countryCode " +
            "AND l.iso in ($languageCodes) " +
            "RETURN  DISTINCT * " +
            "ORDER BY m.weight DESC " +
            "SKIP $skip LIMIT $limit")
    Flux<MovieEntity> findBestByProducerCountry(String countryCode, List<String> languageCodes, int skip, int limit);
}
