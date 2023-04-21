CREATE CONSTRAINT movies_unique_id ON (m:Movie) ASSERT m.movieId IS UNIQUE;
