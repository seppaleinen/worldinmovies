import React from 'react';
import './index.scss';
import Home from './Home';
import MyMoviesMap from "./movies/MyMoviesMap";
import Import from "./import/Import";
import {Provider} from "mobx-react";
import * as serviceWorker from './serviceWorker';
import * as Sentry from '@sentry/browser';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import MovieStore from "./stores/MovieStore";
import {createRoot} from "react-dom/client";
import Header from "./Header";
import TraktImport from "./import/TraktImport";
import ImdbImport from "./import/ImdbImport";
import CountryPage from "./movies/CountryPage";
import MovieDetails from "./movies/MovieDetails";
import Admin from "./admin/Admin";


const sentryUrl = process.env.REACT_APP_SENTRY_URL;
if (sentryUrl !== undefined && sentryUrl.length !== 0) {
    Sentry.init({dsn: sentryUrl});
}

const stores = {
    movieStore: new MovieStore()
}

const Main = () => {
    return (
        <div>
            <Header/>
            <Provider {...stores}>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" index element={<Home />} />
                        <Route path="/map" element={<MyMoviesMap />} />
                        <Route path="/import" element={<Import />} />
                        <Route path="/import/trakt" element={<TraktImport />} />
                        <Route path="/import/imdb" element={<ImdbImport />} />
                        <Route path="/admin" element={<Admin />} />
                        <Route path="/country/:countryCode" element={<CountryPage />} />
                        <Route path="/movie/:movieId" element={<MovieDetails/>} />
                    </Routes>
                </BrowserRouter>
            </Provider>
        </div>
    )
}

const root = createRoot(document.getElementById('root') as Element);
root.render(<Main/>);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
