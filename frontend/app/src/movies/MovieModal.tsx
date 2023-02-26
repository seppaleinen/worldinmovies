import React from 'react';
import {inject, observer} from "mobx-react";
import {Movie, MovieModalState, MyMovie} from "../Types";
import {StoreType} from "../stores/MovieStore";
import {StateStoreType} from "../stores/StateStore";
import './MovieModal.scss';

@inject('movieStore', 'stateStore')
@observer
class MovieModal extends React.Component<Props, MovieModalState> {
    renderTopMovies(data: Movie[]) {
        return (
            data.slice()
                .sort((a: Movie, b: Movie) => (a.vote_average > b.vote_average) ? -1 : 1)
                .map((item: Movie) =>
                    <tr key={item.imdb_id}>
                        <td></td>
                        <td><a href={"https://www.imdb.com/title/" + item.imdb_id} target="_blank"
                               rel="noopener noreferrer">{item.original_title}{item.en_title}</a></td>
                        <td>{item.vote_average}</td>
                    </tr>
                )
        );
    }

    shouldIRenderMyMovies() {
        const movieStore = this.props.movieStore!;
        const stateStore = this.props.stateStore!;
        return movieStore.myMovies !== undefined && Object.keys(movieStore.myMovies).length !== 0 && stateStore.code in movieStore.myMovies;
    }

    renderMyMovies(data: Record<string, MyMovie[]>) {
        let rows = data[this.props.stateStore!.code].slice()
            .sort((a: MyMovie, b: MyMovie) => (a.personal_rating > b.personal_rating) ? -1 : 1)
            .slice(0, 10)
            .map((item: MyMovie) => (
                <tr key={item.imdb_id}>
                    <td></td>
                    <td><a href={"https://www.imdb.com/title/" + item.imdb_id} target="_blank"
                           rel="noopener noreferrer">{item.title}</a></td>
                    <td>{item.personal_rating}</td>
                </tr>
            ));

        return (
            <div id="myMoviesTable"><h2>Your top ranked movies from {this.props.stateStore!.regionName}</h2>
                <table className="modal-table">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Title</th>
                        <th>Your rating</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rows}
                    </tbody>
                </table>
            </div>
        )
    }

    render() {
        return (
            <div id="myModal" className="modal">
                <div className="modal-content">
                    <div id="modal-text">
                        <section id="containingSection">
                            <div id="rankedMoviesTable"><h2>Top ranked movies
                                from {this.props.stateStore!.regionName}</h2>
                                <table className="modal-table">
                                    <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Title</th>
                                        <th>Rating</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {this.renderTopMovies(this.props.movieStore!.movies)}
                                    </tbody>
                                </table>
                            </div>
                            {this.shouldIRenderMyMovies() && this.renderMyMovies(this.props.movieStore!.myMovies)}
                        </section>
                    </div>
                </div>
            </div>
        )
    }
}

export interface Props {
    movieStore?: StoreType;
    stateStore?: StateStoreType;
    data?: MyMovie[];
}

export default MovieModal;