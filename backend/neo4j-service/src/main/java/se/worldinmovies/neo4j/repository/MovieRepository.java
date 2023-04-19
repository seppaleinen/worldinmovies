package se.worldinmovies.neo4j.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import se.worldinmovies.neo4j.entity.MovieEntity;

@Repository
public interface MovieRepository extends ReactiveNeo4jRepository<MovieEntity, Integer> {
    @Transactional
    default Mono<MovieEntity> saveWithCommit(MovieEntity movie) {
        return save(movie);
    }
}
