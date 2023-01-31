import {makeAutoObservable} from "mobx"

export interface StateStoreType {
    showMovieModal: boolean;
    code: string;
    regionName: string;

    showImportModal: boolean;
    importView: string;
    closeImportModal: () => void;
    toggleShowMovieModal: () => void;
    setShowImportModal: (bool: boolean) => void;
    setImportView: (view: string) => void;
}

export default class StateStore implements StateStoreType {
    code = '';
    regionName = '';
    showMovieModal = false;
    showImportModal = false;
    importView = 'FIRST';

    toggleShowMovieModal = () => {
        this.showMovieModal = !this.showMovieModal;
    }

    closeImportModal = () => {
        this.showImportModal = false;
        this.importView = 'FIRST';
    }

    setShowImportModal = (bool: boolean) => {
        this.showImportModal = bool;
    }

    setImportView = (view: string) => {
        this.importView = view;
    }

    constructor() {
        makeAutoObservable(this);
    }
}

