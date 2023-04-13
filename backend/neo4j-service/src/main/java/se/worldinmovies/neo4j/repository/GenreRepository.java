package se.worldinmovies.neo4j.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.worldinmovies.neo4j.entity.GenreEntity;

@Repository
public interface GenreRepository extends ReactiveNeo4jRepository<GenreEntity, Integer> {
}
