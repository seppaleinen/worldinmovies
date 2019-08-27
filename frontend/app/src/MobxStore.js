import { observable, action, decorate } from "mobx"


export default class Store {
  showMovieModal = false;
  showImportModal = false;
  movies = [];
  myMovies = [];
  code = '';
  regionName = '';

  toggleShowMovieModal() {
    this.showMovieModal = !this.showMovieModal;
  }
}

decorate(Store, {
  showMovieModal: observable,
  showImportModal: observable,
  movies: observable,
  myMovies: observable,
  regionName: observable,
  code: observable,
  toggleShowMovieModal: action
});