package se.worldinmovies.neo4j.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;
import se.worldinmovies.neo4j.entity.LanguageEntity;

@Repository
public interface LanguageRepository extends ReactiveNeo4jRepository<LanguageEntity, String> {
}
