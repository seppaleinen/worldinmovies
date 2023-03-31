import styles from "./MovieDetails.module.scss"
import React from "react";

const MovieDetails = (props: Props) => {
    let movie = props.movie;
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
                                    return <div key={name} className={`${styles.country} ${styles.button}`}>{name}</div>
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
                                    return <div key={item.name}>{item.name}</div>
                                })}
                        </div>
                    </div>
                    <div className={styles.bold}>
                        <h4>Writers: </h4>
                        <div className={styles.notbold}>
                            {movie.credits.crew.filter((p: any) => {
                                return p.department === 'Writing'
                            })
                                .map((item: any) => {
                                    return <div key={item.name}>{item.name}</div>
                                })}
                        </div>
                    </div>
                    <div className={styles.bold}>
                        <h4>Ratings: </h4>
                        <div className={styles.notbold}>
                            {Math.round(movie.vote_average)}/10
                        </div>
                    </div>
                    <div className={`${styles.button} ${styles.seenit}`}>I've seen it</div>
                </div>
            </div>

            <div>{movie.runtime} minutes</div>

            <div className={styles.genres}>
                {movie.genres
                    .map((a:any) => {return a.name})
                    .map((a:any) => {return <div key={a} className={styles.button}>{a}</div>})}
            </div>
        </div>

    )
}

export interface Props {
    redirectToPage: (page: string) => void;
    movie: any;
}

export default MovieDetails;