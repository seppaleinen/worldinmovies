package se.worldinmovies.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.*;
import se.worldinmovies.neo4j.domain.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Node(value = "Movie", labels = "Movie")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MovieEntity implements Serializable {
    @Id
    @Property
    private Integer movieId;
    @Property
    private String imdbId;
    @Property
    private String originalTitle;
    @Property
    private String engTitle;
    @Property
    private double weight;
    @Version
    private Long version;
    @Property
    private double voteAverage;
    @Property
    private int voteCount;
    @Property
    private double imdbVoteAverage;
    @Property
    private int imdbVoteCount;
    @Property
    private String posterPath;

    @Relationship(direction = Relationship.Direction.INCOMING, type = "original_language")
    private LanguageRelations originalLanguage;

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
    @Transient
    private String tmpOrigLang;

    public MovieEntity(Integer movieId) {
        this.movieId = movieId;
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
        this.originalLanguage = Optional.ofNullable(this.tmpOrigLang)
                .map(a -> languages.getOrDefault(a, null))
                .map(LanguageRelations::new)
                .orElse(null);
        return this;
    }

    public MovieEntity withCountries(Map<String, CountryEntity> countries) {
        map(this.tmpCountries, countries, Country::getIso)
                .filter(e -> this.producedBy.stream().noneMatch(a -> a.getCountry().getIso().equals(e.getIso())))
                .forEach(e -> this.producedBy.add(new CountryRelations(e)));
        return this;
    }

    public MovieEntity withVotes(List<Votes> votes) {
        Optional.ofNullable(votes)
                .map(v -> v.stream()
                        .filter(a -> a.id() == this.movieId)
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .ifPresent(v -> {
                    this.imdbVoteAverage = v.imdbVoteAverage();
                    this.imdbVoteCount = v.imdbVoteCount();
                    this.weight = v.weightedRating();
                });

        return this;
    }

    private static <D, E, ID> Stream<E> map(List<D> list, Map<ID, E> map, Function<D, ID> getId) {
        return Optional.ofNullable(list).orElse(List.of()).stream()
                .map(d -> Optional.ofNullable(map.get(getId.apply(d))))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public MovieEntity withData(Movie movie) {
        this.movieId = movie.getMovieId();
        this.imdbId = movie.getImdbId();
        this.originalTitle = movie.getOriginalTitle();
        this.engTitle = movie.getEngTitle();
        this.weight = movie.calculateWeightedRating();
        this.voteCount = movie.getVoteCount();
        this.voteAverage = movie.getVoteAverage();
        this.posterPath = movie.getPosterPath();

        this.tmpGenres = movie.getGenres();
        this.tmpLangs = movie.getSpokenLanguages();
        this.tmpCountries = movie.getProducedBy();
        this.tmpOrigLang = movie.getOriginalLanguage();
        return this;
    }
}
