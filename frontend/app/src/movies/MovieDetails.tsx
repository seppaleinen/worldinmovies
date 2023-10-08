import styles from "./MovieDetails.module.scss"
import React, {useEffect, useState} from "react";
import {Link, useParams} from "react-router-dom";
import MovieStore from "../stores/MovieStore";
import {inject, observer} from "mobx-react";
import {checkMark} from "../Svgs";
import {Movie} from "../Types";

const MovieDetails = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore }) => {
    const params = useParams();
    const tmdbUrl = import.meta.env.REACT_APP_TMDB_URL === undefined ? '/tmdb' : import.meta.env.REACT_APP_TMDB_URL;
    const [movie, setMovie] = useState<any>()
    const [hasSeen, setHasSeen] = useState<boolean>(movieStore!.hasSeen(parseInt(params.movieId!)));
    const [active, setActive] = useState("Details");

    useEffect(() => {
        fetch(`${tmdbUrl}/movie/${params.movieId}`)
            .then(response => response.json())
            .then(json => setMovie(json[0]))
            .catch(error => console.error(error));
    }, [params, tmdbUrl]);

    const toggleSeenButton = () => {
        setHasSeen(!hasSeen);
        let store = movieStore!;
        let movieId: number = parseInt(params.movieId!);
        let countries = movie.production_countries.map((a: any) => a.iso_3166_1)
        if (hasSeen) {
            store.removeSeen(countries, movieId);
        } else {
            store.addSeen(countries, movie);
        }
    }

    const isActive = (name: string) => {
        return active === name;
    }

    const changeDetailsTo = (name: string) => {
        setActive(name);
    }

    const createDetailDiv = (name: string) => {
        return <div onClick={() => changeDetailsTo(name)}
                    className={`${styles.detailHeader} ${isActive(name) ? styles.isActive : null}`}>{name}</div>
    }

    if (movie) {
        return (
            <div className={styles.container}>
                <h1 className={styles.title}>{`${movie.original_title} (${movie.release_date.slice(0, 4)})`}</h1>
                {movie.title !== movie.original_title ?
                    <div className={`${styles.title} ${styles.eng_title}`}>´{movie.title}´</div>
                    : null
                }
                <div className={styles.poster}>
                    <img src={`https://image.tmdb.org/t/p/original/${movie.poster_path}`} alt={movie.title}/>
                </div>
                <div className={styles.countries}>
                    {movie.production_countries
                        .map((item: any) => {
                            let iso = item.iso_3166_1 === 'US' ? 'USA' : item.name
                            return <Link key={iso}
                                         className={`${styles.button}`}
                                         to={`/country/${item.iso_3166_1}`}>{iso}
                            </Link>
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
                    <div className={`${styles.notbold} ${styles.rating}`}>
                        <div>TMDB</div>
                        {`${Math.round(movie.vote_average * 10) / 10} / 10`}
                    </div>
                </div>

                <div onClick={() => toggleSeenButton()}
                     className={`${styles.button} ${styles.seenit} ${hasSeen ? styles.seen : null}`}>I've seen
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

                <div className={styles.detailsContainer}>
                    {createDetailDiv("Cast")}
                    {createDetailDiv("Crew")}
                    {createDetailDiv("Details")}
                </div>
                <div className={styles.detailsContent}>
                    {active === "Cast" ? createCastPage(movie) : null}
                    {active === "Crew" ? createCrewPage(movie) : null}
                    {active === "Details" ? createDetailsPage(movie) : null}
                </div>

            </div>
        )
    } else {
        return (
            <div className={styles.container}>Could not get movie data</div>
        )
    }
}))

const createDetailsPage = (movie: Movie) => {
    return <div className={styles.horizontalDetails}>
        <div className={`${styles.values}`}>
            <h4>Studios: </h4>
            <div>
                {movie.production_companies
                    .map((item: any) => {
                        return <div className={`${styles.value}`} key={item.id}>{item.name}</div>
                    })}
            </div>
        </div>
        <div className={`${styles.values}`}>
            <h4>Languages: </h4>
            <div>
                {movie.spoken_languages
                    .map((item: any) => {
                        return <div className={`${styles.value}`} key={item.iso_639_1}>{item.name}</div>
                    })}
            </div>
        </div>
        <div className={`${styles.values}`}>
            <h4>Alternative titles: </h4>
            <div>
                {movie.alternative_titles.titles
                    .map((item: any) => {
                        return <div className={`${styles.value}`}
                                    key={`${item.iso_3166_1}-${item.title}`}>{item.title}</div>
                    })}
            </div>
        </div>
    </div>
}

const createCastPage = (movie: Movie) => {
    return <div className={styles.castPage}>
        <div className={`${styles.values}`}>
            {movie.credits.cast
                .sort((a: any, b: any) => (a.order > b.order) ? 1 : ((b.order > a.order) ? -1 : 0))
                .map((item: any) => {
                    return <div className={`${styles.value}`} key={item.credit_id}>
                        <div>
                            {item.profile_path ? <img src={`https://image.tmdb.org/t/p/w200/${item.profile_path}`}
                                                      alt={item.name}/> : null}
                        </div>
                        <h4>
                            {item.name}
                        </h4>
                        <div>
                            {item.character}
                        </div>
                    </div>
                })}
        </div>
    </div>
}

const createCrewDiv = (title: string, jobTitle: string, groupedCrew: Record<string, any[]>) => {
    return <div className={`${styles.values}`}>
        <h4>{`${title}: `}</h4>
        <div>
            {groupedCrew[jobTitle] ? groupedCrew[jobTitle]
                .map((item: any) => {
                    return <div className={`${styles.value}`} key={item.credit_id}>{item.name}</div>
                }) : null}
        </div>
    </div>;
}

const createCrewPage = (movie: Movie) => {
    const groupedCrew: Record<string, any[]> = movie.credits.crew.reduce((groups: any, item: any) => {
        const group = (groups[item.job] || []);
        group.push(item);
        groups[item.job] = group;
        return groups;
    }, {});

    return <div className={styles.verticalDetails}>
        {createCrewDiv("Producers", "Producer", groupedCrew)}
        {createCrewDiv("Editor", "Editor", groupedCrew)}
        {createCrewDiv("Cinematography", "Director of Photography", groupedCrew)}
        {createCrewDiv('Production Design', 'Production Design', groupedCrew)}
        {createCrewDiv('Set Decoration', 'Set Decoration', groupedCrew)}
        {createCrewDiv('Original Music Composer', 'Original Music Composer', groupedCrew)}
        <div className={`${styles.values}`}>
            <h4>Sound: </h4>
            <div>
                {Object.values(groupedCrew)
                    .flatMap(a => a)
                    .filter(value => ['Sound Designer', 'Sound Recordist', 'Sound Editor', 'Sound Re-Recording Mixer', 'Sound Engineer'].includes(value.job))
                    .map((item: any) => {
                        return <div className={`${styles.value}`} key={item.credit_id}>{item.name}</div>
                    })}
            </div>
        </div>
    </div>
}


export default MovieDetails;