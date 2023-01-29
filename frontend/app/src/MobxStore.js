import {action, makeAutoObservable} from "mobx"
import {makePersistable, startPersisting, isHydrated, clearPersistedStore} from 'mobx-persist-store';

export default class Store {
    showMovieModal = false;
    movies = [];
    myMovies = [];
    code = '';
    regionName = '';

    showImportModal = false;
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

