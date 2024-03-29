import {StoreType} from "./stores/MovieStore";

export interface MyMovieMapState {
    myMovies?: Record<string, MyMovie[]>;
}

export interface Movie {
    credits?: any;
    alternative_titles?: any;
    spoken_languages?: any;
    production_companies?: any;
    id: number;
    imdb_id: string;
    original_title: string;
    release_date: string;
    poster_path: string;
    vote_average: number;
    vote_count: number;
    en_title: string;
    weight: number;
}

export interface MyMovie extends Movie {
    personal_rating: string;
}

export interface RatingsResponse {
    found: Record<string, MyMovie[]>;
    not_found: NotFound[];
}

export interface NotFound {
    title: string;
    year: string;
    imdb_id: string;
}

export interface Props {
    movieStore?: StoreType;
    data?: MyMovie[];
}

export interface MovieModalState {
    toggleRankedMovies: string;
    movies: Movie[];
}

