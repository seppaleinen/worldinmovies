import React, {lazy, Suspense} from 'react';
import './index.scss';
import {Provider} from "mobx-react";
import * as serviceWorker from './serviceWorker';
import * as Sentry from '@sentry/browser';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import MovieStore from "./stores/MovieStore";
import {createRoot} from "react-dom/client";
import Header from "./Header";

const Home = lazy(() => import('./Home'));
const Import = lazy(() => import('./import/Import'));
const ImdbImport = lazy(() => import('./import/ImdbImport'));
const TraktImport = lazy(() => import('./import/TraktImport'));
const MyMoviesMap = lazy(() => import('./movies/MyMoviesMap'));
const MovieDetails = lazy(() => import('./movies/MovieDetails'));
const CountryPage = lazy(() => import('./movies/CountryPage'));
const Admin = lazy(() => import('./admin/Admin'));


const sentryUrl = process.env.REACT_APP_SENTRY_URL;
if (sentryUrl !== undefined && sentryUrl.length !== 0) {
    Sentry.init({dsn: sentryUrl});
}

const stores = {
    movieStore: new MovieStore()
}

const wrapInSuspense = (component: any) => {
    return <Suspense fallback={<div>Loading...</div>}>
        {component}
    </Suspense>
}

const Main = () => {
    return (
        <div>
            <Header/>
            <Provider {...stores}>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" index element={wrapInSuspense(<Home/>)}/>
                        <Route path="/map" element={wrapInSuspense(<MyMoviesMap/>)}/>
                        <Route path="/import" element={wrapInSuspense(<Import/>)}/>
                        <Route path="/import/trakt" element={wrapInSuspense(<TraktImport/>)}/>
                        <Route path="/import/imdb" element={wrapInSuspense(<ImdbImport/>)}/>
                        <Route path="/admin" element={wrapInSuspense(<Admin/>)}/>
                        <Route path="/country/:countryCode" element={wrapInSuspense(<CountryPage/>)}/>
                        <Route path="/movie/:movieId" element={wrapInSuspense(<MovieDetails/>)}/>
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
