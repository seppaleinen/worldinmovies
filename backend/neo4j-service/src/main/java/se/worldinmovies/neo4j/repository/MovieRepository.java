package se.worldinmovies.neo4j.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;
import se.worldinmovies.neo4j.entity.MovieEntity;

@Repository
public interface MovieRepository extends ReactiveNeo4jRepository<MovieEntity, Integer> {
}
