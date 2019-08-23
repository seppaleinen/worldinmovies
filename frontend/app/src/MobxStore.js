import { observable, action, decorate } from "mobx"


export default class Store {
  showMovieModal = false;
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
  movies: observable,
  myMovies: observable,
  regionName: observable,
  code: observable,
  toggleShowMovieModal: action
});