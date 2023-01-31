import {makeAutoObservable} from "mobx"


export interface StateStoreType {
    showMovieModal: boolean;
    code: string;
    regionName: string;

    showImportModal: boolean;
    importView: string;
    closeImportModal: any;
    toggleShowMovieModal: any;
}

export default class StateStore implements StateStoreType {
    showMovieModal = false;
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
    }
}

