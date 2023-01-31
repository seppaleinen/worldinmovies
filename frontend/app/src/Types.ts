import {StoreType} from "./stores/MovieStore";
import {StateStoreType} from "./stores/StateStore";

export interface MyMovieMapState {
    myMovies?: Record<string, MyMovie[]>;
    rerenderModal: number;
    rerenderImportModal: number;
}

export interface Movie {
    imdb_id: string;
    original_title: string;
    release_date: string;
    poster_path: string;
    vote_average: number;
    vote_count: number;
    en_title: string;
}

export interface MyMovie {
    title: string;
    country_code: string;
    year: string;
    imdb_id: string;
    personal_rating: string;
    rating: string;
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
}

export interface MovieModalState {
    rerender?: number;
}

