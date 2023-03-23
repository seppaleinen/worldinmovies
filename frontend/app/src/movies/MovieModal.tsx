import React, {useState} from 'react';
import {inject, observer} from "mobx-react";
import {Movie, MovieModalState, MyMovie} from "../Types";
import MovieStore, {StoreType} from "../stores/MovieStore";
import {StateStoreType} from "../stores/StateStore";
import styles from './MovieModal.module.scss';


@inject('movieStore', 'stateStore')
@observer
class MovieModal extends React.Component<Props, MovieModalState> {

    constructor(props: Props) {
        super(props);
        this.state = {
            toggleRankedMovies: 'best',
        };

    }

    renderTopMovies(store: MovieStore) {
        const data = this.state.toggleRankedMovies === 'best' ? store.movies : store.myMovies[this.props.stateStore!.code];
        return (
            data.slice()
                .sort((a: Movie, b: Movie) => (a.vote_average > b.vote_average) ? -1 : 1)
                .map((item: Movie) =>
                    <div className={styles.movieCard} key={item.imdb_id}>
                        <img className={styles.poster} src={`https://image.tmdb.org/t/p/original/${item.poster_path}`}
                             alt={item.en_title}/>
                        <div className={styles.movieCardText}>
                            <div>{item.original_title} ({item.release_date.slice(0,4)})</div>
                            <div className={styles.englishTitle}>{item.en_title}</div>
                            <div>{item.vote_average}</div>
                        </div>
                    </div>
                )
        );
    }

    handleClick = (newState: string) => {
        this.setState({toggleRankedMovies: newState})
    }

    render() {
        return (
            <div className={styles.container}>
                <div className={styles.title}>
                    <h1>{this.props.stateStore!.regionName}</h1>
                </div>
                <div className={styles.toggle}>
                    <h2 onClick={() => this.handleClick('best')}
                        className={this.state.toggleRankedMovies === 'best' ? styles.activeToggle : styles.inactiveToggle}>Highest
                        rated movies</h2>
                    { this.props.movieStore!.myMovies[this.props.stateStore!.code] ?
                        <h2 onClick={() => this.handleClick('my')}
                        className={this.state.toggleRankedMovies === 'my' ? styles.activeToggle : styles.inactiveToggle}>My
                        highest rated movies</h2> : ''
                    }
                </div>
                <section className={styles.containingSection}>
                    {this.renderTopMovies(this.props.movieStore!)}
                </section>
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