import styles from "./MovieDetails.module.scss"
import React from "react";

const MovieDetails = (props: Props) => {
    let movie = props.movie;
    return (
        <div className={styles.container}>
            <img className={styles.poster} src={`https://image.tmdb.org/t/p/original/${movie.poster_path}`}
                 alt={movie.title}/>
            <div>
                <div className={styles.upperTextArea}>
                    <div className={styles.titles}>
                        <div>{movie.title} ({movie.release_date.slice(0, 4)})</div>
                        <div className={`${styles.country} ${styles.button}`}>{movie.production_countries[0].name}</div>
                    </div>
                    <div className={styles.plot}>{movie.overview}</div>
                    <div className={styles.bold}>
                        <h4>Director: </h4>
                        <div className={styles.notbold}>
                            {movie.credits.crew.filter(function(p:any) {return p.job === 'Director'})
                                .map((item:any) => {return <div key={item.name}>{item.name}</div>})}
                        </div>
                    </div>
                    <div className={styles.bold}>
                        <h4>Writers: </h4>
                        <div className={styles.notbold}>
                            {movie.credits.crew.filter(function(p:any) {return p.department === 'Writing'})
                                .map((item:any) => {return <div key={item.name}>{item.name}</div>})}
                        </div>
                    </div>
                    <div className={styles.bold}>
                        <h4>Ratings: </h4>
                        <div className={styles.notbold}>
                            {movie.vote_average}
                        </div>
                    </div>
                    <div className={`${styles.button}`}>I've seen it</div>
                </div>
            </div>
        </div>
    )
}

export interface Props {
    redirectToPage: (page: string) => void;
    movie: any;
}

export default MovieDetails;