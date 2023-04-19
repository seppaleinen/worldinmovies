package se.worldinmovies.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Node("Movie")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MovieEntity implements Serializable {
    @Id
    private Integer movieId;
    @Property
    private String imdbId;
    @Property
    private String originalTitle;
    @Property
    private String engTitle;
    @Version
    private Long version;

    @Relationship(direction = Relationship.Direction.INCOMING, type = "spoken_languages")
    private List<LanguageRelations> spokenLanguages = new ArrayList<>();

    @Relationship(direction = Relationship.Direction.OUTGOING, type = "produced_by")
    private List<CountryRelations> producedBy = new ArrayList<>();

    @Relationship(direction = Relationship.Direction.INCOMING, type = "genre")
    private List<GenreRelations> genres = new ArrayList<>();

    @Transient
    private List<Genre> tmpGenres = new ArrayList<>();
    @Transient
    private List<Language> tmpLangs = new ArrayList<>();
    @Transient
    private List<Country> tmpCountries = new ArrayList<>();

    public MovieEntity(Integer movieId) {
        this.movieId = movieId;
    }

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
                .filter(e -> this.genres.stream().noneMatch(a -> a.getGenre().getId().equals(e.getId())))
                .forEach(e -> this.genres.add(new GenreRelations(e)));

        return this;
    }

    public MovieEntity withLanguages(Map<String, LanguageEntity> languages) {
        map(this.tmpLangs, languages, Language::getIso)
                .filter(e -> this.spokenLanguages.stream().noneMatch(a -> a.getLanguage().getIso().equals(e.getIso())))
                .forEach(e -> this.spokenLanguages.add(new LanguageRelations(e)));

        return this;
    }

    public MovieEntity withCountries(Map<String, CountryEntity> countries) {
        map(this.tmpCountries, countries, Country::getIso)
                .filter(e -> this.producedBy.stream().noneMatch(a -> a.getCountry().getIso().equals(e.getIso())))
                .forEach(e -> this.producedBy.add(new CountryRelations(e)));
        return this;
    }
    private static <D, E, ID> Stream<E> map(List<D> list, Map<ID, E> map, Function<D, ID> getId) {
        return Optional.ofNullable(list).orElse(List.of()).stream()
                .map(a -> Optional.ofNullable(map.get(getId.apply(a))))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
