import {makeAutoObservable} from "mobx"
import {makePersistable, startPersisting, isHydrated, clearPersistedStore} from 'mobx-persist-store';
import {Movie, MyMovie, StoreType} from "./Types";

export default class Store implements StoreType {
    showMovieModal = false;
    movies: Movie[] = [];
    myMovies: Record<string, MyMovie[]> = {};
    code = '';
    regionName = '';

    showImportModal: boolean = false;
    importView = 'FIRST';

    toggleShowMovieModal = () => {
        this.showMovieModal = !this.showMovieModal;
    }

    closeImportModal = () => {
        this.showImportModal = false;
        this.importView = 'FIRST';
    }

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

    get isHydrated() {
        return isHydrated(this);
    }
}

