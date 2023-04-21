CREATE INDEX movies_index_id FOR (n:Movie) ON (n.movieId);
// CREATE CONSTRAINT movie_unique_imdb_id FOR (n:Movie) REQUIRE n.imdbId IS UNIQUE;

CREATE INDEX country_index_id FOR (n:Country) ON (n.iso);
CREATE INDEX language_index_id FOR (n:Language) ON (n.iso);
CREATE INDEX genre_index_id FOR (n:Genre) ON (n.id);
