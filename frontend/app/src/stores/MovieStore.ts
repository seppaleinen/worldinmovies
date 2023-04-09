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
}

