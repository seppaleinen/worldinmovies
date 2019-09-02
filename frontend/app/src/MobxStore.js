import { observable, action, decorate } from "mobx"


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
}

decorate(Store, {
  showMovieModal: observable,
  showImportModal: observable,
  importView: observable,
  movies: observable,
  myMovies: observable,
  regionName: observable,
  code: observable,
  toggleShowMovieModal: action,
  closeImportModal: action
});