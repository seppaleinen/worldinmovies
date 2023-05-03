import React, {useEffect, useState} from 'react';
import {inject, observer} from "mobx-react";
import {Movie, MyMovie} from "../Types";
import MovieStore, {StoreType} from "../stores/MovieStore";
import styles from './CountryPage.module.scss';
import axios, {AxiosResponse} from "axios";
import {Link} from "react-router-dom";
import {useParams} from "react-router-dom";
import customWorldMapJson from './customworldmap.json';
import InfiniteScroll from 'react-infinite-scroll-component';

const limit = 20;

const CountryPage = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore }) => {

    const neoUrl = process.env.REACT_APP_NEO_URL === undefined ? '/neo' : process.env.REACT_APP_NEO_URL;
    const params = useParams();
    const [toggleRankedMovies, setToggleRankedMovies] = useState<string>('best')
    const [movies, setMovies] = useState<Movie[]>([])
    const [skip, setSkip] = useState<number>(0);

    useEffect(() => {
        fetchData();
    }, [toggleRankedMovies]);

    const fetchData = () => {
        if (toggleRankedMovies === 'best') {
            axios.get(`${neoUrl}/view/best/${params.countryCode!.toUpperCase()}?skip=${skip}&limit=${limit}`, {timeout: 10000})
                .then((response: AxiosResponse) => {
                    setMovies(prevState => prevState.concat(response.data));
                    setSkip(skip + response.data.length);
                })
                .catch(function (error: any) {
                    console.log(error);
                });
        } else {
            let fetched = movieStore!.myMovies[params.countryCode!].slice()
                .sort((a: Movie, b: Movie) => (a.weight > b.weight) ? -1 : 1)
                .slice(skip, (skip + limit));
            setSkip(skip + fetched.length);
            setMovies(prevState => prevState.concat(fetched));
        }
    };

    const renderTopMovies = () => {
        return (
            <InfiniteScroll
                dataLength={movies.length}
                next={fetchData}
                hasMore={true}
                loader={<h4>Loading...</h4>}
            >
                <section className={styles.containingSection}>
                    {movies
                        .sort((a: Movie, b: Movie) => (a.weight > b.weight) ? -1 : 1)
                        .map((item: Movie) =>
                            <Link to={`/movie/${item.id}`} key={item.id ? item.id : item.imdb_id} className={styles.movieCard}>
                                <img className={styles.poster}
                                     src={`https://image.tmdb.org/t/p/w200/${item.poster_path}`}
                                     alt={item.en_title}/>
                                <div className={styles.movieCardText}>
                                    <div>{item.original_title} {item.release_date ? "(" + item.release_date.slice(0, 4) + ")" : null}</div>
                                    {item.en_title && item.en_title.trim() !== item.original_title.trim() ?
                                        <div className={styles.englishTitle}>'{item.en_title}'</div> : null}
                                    <div>{item.vote_average}</div>
                                </div>
                            </Link>
                        )}
                </section>
            </InfiniteScroll>
        );
    }

    const handleClick = (newState: string) => {
        setToggleRankedMovies(newState);
        setMovies([]);
        setSkip(0);
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
            {renderTopMovies()}
        </div>
    )

}));

export interface Props {
    movieStore?: StoreType;
    data?: MyMovie[];
}

export default CountryPage;