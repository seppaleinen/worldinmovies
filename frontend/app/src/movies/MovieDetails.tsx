import styles from "./MovieDetails.module.scss"
import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import axios, {AxiosResponse} from "axios";
import MovieStore from "../stores/MovieStore";
import {inject, observer} from "mobx-react";
import {checkMark} from "../Svgs";

const MovieDetails = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore }) => {
    const params = useParams();
    const tmdbUrl = process.env.REACT_APP_TMDB_URL === undefined ? '/tmdb' : process.env.REACT_APP_TMDB_URL;
    const [movie, setMovie] = useState<any>()
    const [seen, setSeen] = useState<boolean>(movieStore!.hasSeen(params.movieId!));

    useEffect(() => {
        axios.get(tmdbUrl + "/movie/" + params.movieId, {timeout: 5000})
            .then((response: AxiosResponse) => {
                setMovie(response.data[0])
            })
            .catch(function (error: any) {
                console.error(error);
            });
    }, [params, tmdbUrl]);

    const seenit = () => {
        console.log("CLICKED");
        setSeen(!seen);
        let store = movieStore!;
        let movieId = params.movieId!;
        let countries = movie.production_countries.map((a: any) => a.iso_3166_1)
        if(seen) {
            store.removeSeen(countries, movieId);
        } else {
            store.addSeen(countries, movie);
        }
    }

    if (movie) {
        const seenClass = seen ? styles.seen : styles.notseen;
        return (
            <div className={styles.container}>
                <div className={styles.upperArea}>
                    <img className={styles.poster} src={`https://image.tmdb.org/t/p/original/${movie.poster_path}`}
                         alt={movie.title}/>
                    <div className={styles.upperTextArea}>
                        <div className={styles.titles}>
                            <div className={styles.title}>{movie.title} ({movie.release_date.slice(0, 4)})</div>
                            <div className={styles.countries}>
                                {movie.production_countries
                                    .map((item: any) => {
                                        return item.iso_3166_1 === 'US' ? 'USA' : item.name
                                    })
                                    .map((name: string) => {
                                        return <div key={name}
                                                    className={`${styles.country} ${styles.button}`}>{name}</div>
                                    })}
                            </div>
                        </div>
                        <div className={styles.plot}>{movie.overview}</div>
                        <div className={styles.bold}>
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
                        <div className={styles.bold}>
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
                        <div className={styles.bold}>
                            <h4>Ratings: </h4>
                            <div className={styles.notbold}>
                                {Math.round(movie.vote_average)}/10
                            </div>
                        </div>

                        <div onClick={() => seenit()}
                             className={`${styles.button} ${styles.seenit} ${seenClass}`}>I've seen it{seen ? checkMark() : null}</div>
                    </div>
                </div>

                <div>{movie.runtime} minutes</div>

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