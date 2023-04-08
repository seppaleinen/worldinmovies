import React from 'react';
import {inject, observer} from "mobx-react";
import {Movie, MovieModalState, MyMovie} from "../Types";
import MovieStore, {StoreType} from "../stores/MovieStore";
import {StateStoreType} from "../stores/StateStore";
import styles from './CountryPage.module.scss';
import axios, {AxiosResponse} from "axios";


@inject('movieStore', 'stateStore')
@observer
class CountryPage extends React.Component<Props, MovieModalState> {
    tmdbUrl = process.env.REACT_APP_TMDB_URL === undefined ? '/tmdb' : process.env.REACT_APP_TMDB_URL;

    constructor(props: Props) {
        super(props);
        this.state = {
            toggleRankedMovies: 'best',
        };

    }

    renderTopMovies = (store: MovieStore) => {
        const data = this.state.toggleRankedMovies === 'best' ?
            store.movies :
            store.myMovies[this.props.stateStore!.code];
        return (
            data.slice()
                .sort((a: Movie, b: Movie) => (a.vote_average > b.vote_average) ? -1 : 1)
                .map((item: Movie) =>
                    <div className={styles.movieCard} key={item.id === undefined ? item.imdb_id : item.id} onClick={() => this.getDetails(item.id)}>
                        <img className={styles.poster} src={`https://image.tmdb.org/t/p/w200/${item.poster_path}`}
                             alt={item.en_title}/>
                        <div className={styles.movieCardText}>
                            <div>{item.original_title} ({item.release_date.slice(0, 4)})</div>
                            {item.en_title ? <div className={styles.englishTitle}>'{item.en_title}'</div> : null}
                            <div>{item.vote_average}</div>
                        </div>
                    </div>
                )
        );
    }

    handleClick = (newState: string) => {
        this.setState({toggleRankedMovies: newState})
    }

    getDetails = (id: string) => {
        axios.get(this.tmdbUrl + "/movie/" + id, {timeout: 5000})
            .then((response: AxiosResponse) => {
                this.props.redirectToPage("movie-details");
                this.props.setMovie(response.data[0]);
            })
            .catch(function (error: any) {
                console.error(error);
            });
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
                    {this.props.movieStore!.myMovies[this.props.stateStore!.code] ?
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
    redirectToPage: (page: string) => void;
    setMovie: (movie: any) => void;
    movieStore?: StoreType;
    stateStore?: StateStoreType;
    data?: MyMovie[];
}

export default CountryPage;