import React, {Ref, useCallback, useEffect, useState} from 'react';
import {inject, observer} from "mobx-react";
import {Movie, MyMovie} from "../Types";
import MovieStore, {StoreType} from "../stores/MovieStore";
import styles from './CountryPage.module.scss';
import {Link} from "react-router-dom";
import {useParams} from "react-router-dom";
import customWorldMapJson from './countrycodes.json';
import genresJson from './genres.json';
import InfiniteScroll from 'react-infinite-scroll-component';
import {closeIcon, filterIcon} from "../Svgs";
import Select, {InputActionMeta} from 'react-select';

const limit = 20;
const neoUrl = process.env.REACT_APP_NEO_URL === undefined ? '/neo' : process.env.REACT_APP_NEO_URL;

const sortMovies = () => {
    return (a: Movie, b: Movie) => {
        if (a.weight > b.weight) return -1;
        if (a.weight > b.weight) return 1;
        // If weight is the same. sort on votecount
        return a.vote_count > b.vote_count ? -1 : 1;
    };
}

const CountryPage = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore }) => {

    const dialogRef: Ref<HTMLDialogElement> = React.createRef();
    const params = useParams();

    const [toggleRankedMovies, setToggleRankedMovies] = useState<string>('best')
    const [movies, setMovies] = useState<Movie[]>([])
    const [skip, setSkip] = useState<number>(0);
    const [showModal, setShowModal] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [chosenGenres, setChosenGenres] = useState<number[]>([])


    useEffect(() => {
        fetchData();
    }, [toggleRankedMovies, chosenGenres]);

    const closeWhenClickOutsideDialog = useCallback((event: any) => {
        const dialog = document.getElementById("dialog") as HTMLDialogElement;
        const dialogIcon = document.getElementById("dialog-svg") as HTMLElement;
        const clickedElement = event.target as HTMLElement;
        setShowModal((show) => {
            if (show && !dialog.contains(clickedElement.parentElement) && !dialogIcon.contains(clickedElement) && !clickedElement.id.includes("react-select-2")) {
                console.log("Did not contain")
                return false;
            }
            return show;
        });
    }, [setShowModal])

    useEffect(() => {
        if (showModal) {
            document.addEventListener("click", closeWhenClickOutsideDialog, false);
        }
        const dialogElement = document.getElementById("dialog") as HTMLDialogElement;
        showModal ? dialogElement.show() : dialogElement.close();
        return () => document.removeEventListener("click", closeWhenClickOutsideDialog, false);

    }, [showModal, closeWhenClickOutsideDialog]);

    const fetchData = () => {
        const genres = chosenGenres.length > 0 ? `&genres=${chosenGenres}` : "";
        if (toggleRankedMovies === 'best') {
            fetch(`${neoUrl}/view/best/${params.countryCode!.toUpperCase()}?skip=${skip}&limit=${limit}${genres}`,
                {
                    signal: AbortSignal.timeout(10000)
                })
                .then(resp => resp.json())
                .then(response => handleResults(response))
                .catch(error => console.log(error));
        } else {
            handleResults(movieStore!.myMovies[params.countryCode!].slice()
                .sort(sortMovies())
                .slice(skip, (skip + limit)));
        }
    };

    const handleResults = (result: any[]) => {
        setSkip(skip + result.length);
        setMovies(prevState => prevState.concat(result));
        setHasMore(result.length >= limit);
    }

    const renderTopMovies = () => {
        return (
            <InfiniteScroll
                dataLength={movies.length}
                next={() => fetchData()}
                hasMore={hasMore}
                loader={<h4>Loading...</h4>}
            >
                <section className={styles.containingSection}>
                    {movies.length > 0 ? movies
                        .sort(sortMovies())
                        .map((item: Movie) =>
                            <Link to={`/movie/${item.id}`} key={item.id ? item.id : item.imdb_id}
                                  className={styles.movieCard}>
                                {item.poster_path ? <img className={styles.poster}
                                                         src={`https://image.tmdb.org/t/p/w200/${item.poster_path}`}
                                                         alt={item.en_title}/> : null}
                                <div className={styles.movieCardText}>
                                    <div>{item.original_title} {item.release_date ? "(" + item.release_date.slice(0, 4) + ")" : null}</div>
                                    {item.en_title && item.en_title.trim() !== item.original_title.trim() ?
                                        <div className={styles.englishTitle}>'{item.en_title}'</div> : null}
                                    <div>{item.vote_average}</div>
                                </div>
                            </Link>
                        ) : <div>Could not find any movies</div>}
                </section>
            </InfiniteScroll>
        );
    }

    const handleClick = (newState: string) => {
        if (newState !== toggleRankedMovies) {
            setMovies([]);
            setSkip(0);
            setToggleRankedMovies(newState);
            setHasMore(true);
        }
    }

    const clickyFilter = () => {
        setShowModal(!showModal);
    }

    const onChange = (newValue: any) => {
        setChosenGenres(prevState => {
            const newState = newValue.map((a: any) => Number(a.value));
            if(newState !== prevState) {
                setMovies([]);
                setSkip(0);
                setHasMore(true);
            }
            return newState;
        });
    }

    const createGenresDropdown = () => {
        const option = genresJson
            .map(a => JSON.parse(`{"value": "${a.id}", "label": "${a.name}"}`))
        return (
            <Select
                defaultValue={[]}
                name="genres"
                isMulti
                isClearable
                isSearchable
                hideSelectedOptions
                options={option}
                controlShouldRenderValue
                onChange={onChange}
                placeholder={"Choose genres"}
                className="basic-multi-select"
                classNamePrefix="select"
            />
        )
    }

    return (
        <div className={styles.container}>
            <div className={styles.title}>
                <h1>{String(customWorldMapJson[params.countryCode! as keyof Object])}</h1>
            </div>
            <div className={styles.toggle}>
                <h2 onClick={() => handleClick('best')}
                    className={toggleRankedMovies === 'best' ? styles.activeToggle : styles.inactive}>Highest
                    rated movies</h2>
                {movieStore!.myMovies[params.countryCode!] ?
                    <h2 onClick={() => handleClick('my')}
                        className={toggleRankedMovies === 'my' ? styles.activeToggle : styles.inactiveToggle}>My
                        highest rated movies</h2> : null
                }
            </div>
            <div className={styles.filters}>
                <div id={"dialog-svg"} onClick={() => clickyFilter()} className={styles.filter}>{filterIcon()}</div>
                <dialog id={"dialog"} ref={dialogRef}>
                    <div className={styles.closeDialog} onClick={() => clickyFilter()}>{closeIcon()}</div>
                    <div className={styles.genres}>
                        {createGenresDropdown()}
                    </div>
                </dialog>
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