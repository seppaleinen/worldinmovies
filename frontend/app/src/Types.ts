import {StoreType} from "./stores/MovieStore";
import {StateStoreType} from "./stores/StateStore";

export interface MyMovieMapState {
    myMovies?: Record<string, MyMovie[]>;
}

export interface Movie {
    id: string;
    imdb_id: string;
    original_title: string;
    release_date: string;
    poster_path: string;
    vote_average: number;
    vote_count: number;
    en_title: string;
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
    stateStore?: StateStoreType;
    data?: MyMovie[];
    redirectToPage: (page: string) => void;
}

export interface MovieModalState {
    toggleRankedMovies: string;
}

