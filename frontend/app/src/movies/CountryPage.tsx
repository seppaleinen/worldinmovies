import React, {useEffect, useState} from 'react';
import {inject, observer} from "mobx-react";
import {Movie, MyMovie} from "../Types";
import MovieStore, {StoreType} from "../stores/MovieStore";
import styles from './CountryPage.module.scss';
import axios, {AxiosResponse} from "axios";
import {useNavigate} from "react-router-dom";
import {useParams} from "react-router-dom";
import customWorldMapJson from './customworldmap.json';

const CountryPage = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore }) => {

    const navigate = useNavigate();
    const backendUrl = process.env.REACT_APP_BACKEND_URL === undefined ? '/backend' : process.env.REACT_APP_BACKEND_URL;
    const params = useParams();
    const [toggleRankedMovies, setToggleRankedMovies] = useState<string>('best')
    const [movies, setMovies] = useState<Movie[]>([])

    useEffect(() => {
        axios.get(backendUrl + "/view/best/" + params.countryCode!.toUpperCase(), {timeout: 10000})
            .then((response: AxiosResponse) => {
                setMovies(response.data.result);
            })
            .catch(function (error: any) {
                console.log(error);
            });
    })

    const renderTopMovies = (store: MovieStore) => {
        const data = toggleRankedMovies === 'best' ?
            movies :
            store.myMovies[params.countryCode!];
        return (
            data.slice()
                .sort((a: Movie, b: Movie) => (a.vote_average > b.vote_average) ? -1 : 1)
                .map((item: Movie) =>
                    <div className={styles.movieCard} key={item.id === undefined ? item.imdb_id : item.id}
                         onClick={() => navigate("/movie/" + item.id)}>
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

    const handleClick = (newState: string) => {
        setToggleRankedMovies(newState);
    }

    return (
        <div className={styles.container}>
            <div className={styles.title}>
                <h1>{customWorldMapJson.content.paths[params.countryCode! as keyof Object]?.name}</h1>
            </div>
            <div className={styles.toggle}>
                <h2 onClick={() => handleClick('best')}
                    className={toggleRankedMovies === 'best' ? styles.activeToggle : styles.inactiveToggle}>Highest
                    rated movies</h2>
                {movieStore!.myMovies[params.countryCode!] ?
                    <h2 onClick={() => handleClick('my')}
                        className={toggleRankedMovies === 'my' ? styles.activeToggle : styles.inactiveToggle}>My
                        highest rated movies</h2> : ''
                }
            </div>
            <section className={styles.containingSection}>
                {renderTopMovies(movieStore!)}
            </section>
        </div>
    )

}));

export interface Props {
    movieStore?: StoreType;
    data?: MyMovie[];
}

export default CountryPage;