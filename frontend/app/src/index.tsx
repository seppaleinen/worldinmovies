import React from 'react';
import './index.scss';
import App from './App';
import Admin from './admin/Admin';
import {Provider} from "mobx-react";
import * as serviceWorker from './serviceWorker';
import * as Sentry from '@sentry/browser';
import {BrowserRouter, Routes, Route} from "react-router-dom";
import MovieStore from "./stores/MovieStore";
import StateStore from "./stores/StateStore";
import {createRoot} from "react-dom/client";

const sentryUrl = process.env.REACT_APP_SENTRY_URL;
if (sentryUrl !== undefined && sentryUrl.length !== 0) {
    Sentry.init({dsn: sentryUrl});
}

const stores = {
    movieStore: new MovieStore(),
    stateStore: new StateStore()
}

const Main = () =>
    <Provider {...stores}>
        <BrowserRouter>
            <Routes>
                <Route path="/">
                    <Route index element={<App/>}/>
                </Route>
                <Route path="/admin">
                    <Route index element={<Admin/>}/>
                </Route>
            </Routes>
        </BrowserRouter>
    </Provider>

const root = createRoot(document.getElementById('root') as Element);
root.render(<Main/>);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
