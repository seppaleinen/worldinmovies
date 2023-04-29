import {action, makeAutoObservable} from "mobx"
import {makePersistable, startPersisting, hydrateStore} from 'mobx-persist-store';
import {Movie, MyMovie} from "../Types";

export interface StoreType {
    movies: Movie[];
    myMovies: Record<string, MyMovie[]>;
    startStore: () => void;
    hydrateStore: () => Promise<void>;
    setMovie: (movies: Movie[]) => void;
}

export default class MovieStore implements StoreType {
    movies: Movie[] = [];
    myMovies: Record<string, MyMovie[]> = {};

    constructor() {
        makeAutoObservable(this);
        makePersistable(this, {
            name: 'MovieStore',
            properties: ['movies', "myMovies"],
            storage: window.localStorage
        });
    }

    @action
    setMovie(movies: Movie[]) {
        this.movies = movies;
    }

    @action
    startStore() {
        startPersisting(this);
    }

    @action
    async hydrateStore() {
        await hydrateStore(this);
    }

    @action
    hasSeen(movieId: string) {
        return Object.values(this.myMovies)
            .flatMap(a => a)
            .some(a => movieId == a.id);
    }

    @action
    removeSeen(countries: string[], movieId: string) {
        countries.forEach(country => {
            this.myMovies[country] = this.myMovies[country]
                .filter(movie => movie.id != movieId);
        });
    }

    @action
    addSeen(countries: string[], movie: Movie) {
        countries.forEach(country => {
            let movies = this.myMovies[country] ? this.myMovies[country] : [];
            movies.push({
                "id": movie.id,
                "imdb_id": movie.imdb_id,
                "original_title": movie.original_title,
                "release_date": movie.release_date,
                "poster_path": movie.poster_path,
                "vote_average": movie.vote_average,
                "vote_count": movie.vote_count,
                "en_title": movie.en_title,
                "weight": movie.weight,
                "personal_rating": ""
            })
            this.myMovies[country] = movies;
        })
    }
}

