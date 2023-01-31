import {makeAutoObservable} from "mobx"
import {makePersistable, startPersisting, isHydrated, clearPersistedStore, hydrateStore} from 'mobx-persist-store';
import {Movie, MyMovie} from "../Types";

export interface StoreType {
    movies: Movie[];
    myMovies: Record<string, MyMovie[]>;
    startStore: any;
    hydrateStore: () => Promise<void>;
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

    startStore() {
        startPersisting(this);
    }

    async clearStoredData() {
        await clearPersistedStore(this);
    }

    async hydrateStore() {
        await hydrateStore(this);
    }
}

