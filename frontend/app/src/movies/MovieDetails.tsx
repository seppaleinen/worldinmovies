import styles from "./MovieDetails.module.scss"
import React, {useEffect, useState} from "react";
import {NavLink, useParams} from "react-router-dom";
import axios, {AxiosResponse} from "axios";
import MovieStore from "../stores/MovieStore";
import {inject, observer} from "mobx-react";
import {checkMark} from "../Svgs";

const MovieDetails = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore }) => {
    const params = useParams();
    const tmdbUrl = process.env.REACT_APP_TMDB_URL === undefined ? '/tmdb' : process.env.REACT_APP_TMDB_URL;
    const [movie, setMovie] = useState<any>()
    const [hasSeen, setHasSeen] = useState<boolean>(movieStore!.hasSeen(params.movieId!));

    useEffect(() => {
        axios.get(tmdbUrl + "/movie/" + params.movieId, {timeout: 5000})
            .then((response: AxiosResponse) => {
                setMovie(response.data[0])
            })
            .catch(function (error: any) {
                console.error(error);
            });
    }, [params, tmdbUrl]);

    const toggleSeenButton = () => {
        setHasSeen(!hasSeen);
        let store = movieStore!;
        let movieId = params.movieId!;
        let countries = movie.production_countries.map((a: any) => a.iso_3166_1)
        if (hasSeen) {
            store.removeSeen(countries, movieId);
        } else {
            store.addSeen(countries, movie);
        }
    }

    if (movie) {
        const seenClass = hasSeen ? styles.seen : styles.notseen;
        return (
            <div className={styles.container}>
                <div className={styles.poster}>
                    <img src={`https://image.tmdb.org/t/p/original/${movie.poster_path}`}
                         alt={movie.title}/>
                </div>
                <h1 className={styles.title}>{`${movie.original_title} (${movie.release_date.slice(0, 4)})`}</h1>
                <div className={`${styles.title} ${styles.eng_title}`}>´{movie.title}´</div>
                <div className={styles.countries}>
                    {movie.production_countries
                        .map((item: any) => {
                            let iso = item.iso_3166_1 === 'US' ? 'USA' : item.name
                            return <NavLink key={iso}
                                            className={`${styles.button}`}
                                            to={`/country/${item.iso_3166_1}`}>{iso}
                            </NavLink>
                        })}
                </div>
                <div className={styles.plot}>{movie.overview}</div>
                <div className={`${styles.bold} ${styles.directors}`}>
                    <h4>Director: </h4>
                    <div className={styles.notbold}>
                        {movie.credits.crew.filter((p: any) => {
                            return p.job === 'Director'
                        })
                            .map((item: any) => {
                                return <div key={item.credit_id}>{item.name}</div>
                            })}
                    </div>
                </div>
                <div className={`${styles.bold} ${styles.writers}`}>
                    <h4>Writers: </h4>
                    <div className={styles.notbold}>
                        {movie.credits.crew
                            .filter((p: any) => {
                                return p.department === 'Writing'
                            })
                            .map((item: any) => {
                                return <div key={item.credit_id}>{item.name} ({item.job})</div>
                            })}
                    </div>
                </div>
                <div className={`${styles.bold} ${styles.ratings}`}>
                    <h4>Ratings: </h4>
                    <div className={styles.notbold}>
                        {Math.round(movie.vote_average)}/10
                    </div>
                </div>

                <div onClick={() => toggleSeenButton()}
                     className={`${styles.button} ${styles.seenit} ${seenClass}`}>I've seen
                    it{hasSeen ? checkMark() : null}
                </div>

                <div className={styles.runtime}>{movie.runtime} minutes</div>

                <div className={styles.genres}>
                    {movie.genres
                        .map((a: any) => {
                            return a.name
                        })
                        .map((a: any) => {
                            return <div key={a} className={styles.button}>{a}</div>
                        })}
                </div>
            </div>

        )
    } else {
        return (
            <div className={styles.container}>Could not get movie data</div>
        )
    }
}))


export default MovieDetails;