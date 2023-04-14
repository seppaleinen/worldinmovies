package se.worldinmovies.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import se.worldinmovies.neo4j.domain.Country;
import se.worldinmovies.neo4j.domain.Genre;
import se.worldinmovies.neo4j.domain.Language;
import se.worldinmovies.neo4j.domain.Movie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Node("Movie")
@Data
@NoArgsConstructor
public class MovieEntity implements Serializable {
    @Id
    private Integer movieId;
    @Property
    private String imdbId;
    @Property
    private String originalTitle;
    @Property
    private String engTitle;

    @Relationship(direction = Relationship.Direction.OUTGOING, type = "spoken_languages")
    private List<LanguageEntity> spokenLanguages = new ArrayList<>();

    @Relationship(direction = Relationship.Direction.OUTGOING, type = "produced_by")
    private List<CountryEntity> producedBy = new ArrayList<>();

    @Relationship(direction = Relationship.Direction.OUTGOING)
    private List<GenreEntity> genres = new ArrayList<>();

    @Transient
    private List<Genre> tmpGenres = new ArrayList<>();
    @Transient
    private List<Language> tmpLangs = new ArrayList<>();
    @Transient
    private List<Country> tmpCountries = new ArrayList<>();

    public MovieEntity(Movie newMovie) {
        this.movieId = newMovie.getMovieId();
        this.imdbId = newMovie.getImdbId();
        this.originalTitle = newMovie.getOriginalTitle();
        this.engTitle = newMovie.getEngTitle();

        this.tmpGenres = newMovie.getGenres();
        this.tmpLangs = newMovie.getSpokenLanguages();
        this.tmpCountries = newMovie.getProducedBy();
    }

    public MovieEntity withGenres(Map<Integer, GenreEntity> genres) {
        map(this.tmpGenres, genres, Genre::getId)
                .forEach(genre -> this.genres.add(genre));
        return this;
    }

    public MovieEntity withLanguages(Map<String, LanguageEntity> languages) {
        map(this.tmpLangs, languages, Language::getIso)
                .forEach(lang -> this.spokenLanguages.add(lang));
        return this;
    }

    public MovieEntity withCountries(Map<String, CountryEntity> countries) {
        map(this.tmpCountries, countries, Country::getIso)
                .forEach(country -> this.producedBy.add(country));
        return this;
    }

    public <D, E, ID> Stream<E> map(List<D> list, Map<ID, E> countries, Function<D, ID> getId) {
        return list.stream()
                .map(a -> Optional.ofNullable(countries.get(getId.apply(a))))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public MovieEntity updateWith(Movie newData, Map<Integer, GenreEntity> genres, Map<String, LanguageEntity> languages, Map<String, CountryEntity> countries) {
        this.imdbId = newData.getImdbId();
        this.originalTitle = newData.getOriginalTitle();
        this.engTitle = newData.getEngTitle();
        newData.getGenres().stream()
                .map(a -> genres.get(a.getId()))
                .forEach(a -> this.genres.add(a));
        newData.getSpokenLanguages().stream()
                .map(a -> languages.get(a.getIso()))
                .forEach(a -> this.spokenLanguages.add(a));
        newData.getProducedBy().stream()
                .map(a -> countries.get(a.getIso()))
                .forEach(a -> this.producedBy.add(a));
        return this;
    }
}
