import React from 'react';
import {inject, observer} from "mobx-react";
import {MovieModalState, MyMovie, Props} from "./Types";

@inject('movieStore', 'stateStore')
@observer
class MovieModal extends React.Component<Props, MovieModalState> {
    state: MovieModalState = {};

    constructor(props: Props) {
        super(props);
        this.state = {rerender: Math.random()};
    }


    renderTopMovies(data: any) {
        return (
            data.map((item: any) =>
                <tr key={item['imdb_id']}>
                    <td></td>
                    <td><a href={"https://www.imdb.com/title/" + item['imdb_id']} target="_blank"
                           rel="noopener noreferrer">{item['original_title']}{item['en_title']}</a></td>
                    <td>{item['vote_average']}</td>
                </tr>
            )
        )
    }

    componentDidUpdate(prevProps: Props, prevState: MovieModalState) {
        if (this.state.rerender !== prevState.rerender) {
            this.setState({rerender: this.state.rerender});
        }
        if (this.props.stateStore!.showMovieModal !== prevProps.stateStore!.showMovieModal) {
            this.setState({rerender: this.state.rerender});
        }
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

    componentDidMount() {
        // When the user clicks on <span> (x), close the modal
        document.getElementById("closeMovieModalButton")!.onclick = () => {
            this.props.stateStore!.toggleShowMovieModal();
        }
    }

    render() {
        const showModal = this.props.stateStore!.showMovieModal ? 'block' : 'none';
        return (
            <div id="myModal" className="modal" style={{display: showModal}}>
                <div className="modal-content">
                    <span id="closeMovieModalButton" className='close movieModalClose'>&times;</span>
                    <div id="modal-text">
                        <section id="containingSection">
                            <div id="rankedMoviesTable"><h2>Top ranked movies from {this.props.stateStore!.regionName}</h2>
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

export default MovieModal;