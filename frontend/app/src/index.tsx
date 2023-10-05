import React, { lazy, Suspense } from 'react';
import './index.scss';
import { Provider } from "mobx-react";
import * as serviceWorker from './serviceWorker';
import {
    BrowserRouter,
    createRoutesFromChildren,
    matchRoutes,
    Route,
    Routes, useLocation,
    useNavigationType
} from "react-router-dom";
import MovieStore from "./stores/MovieStore";
import { createRoot } from "react-dom/client";
import Header from "./Header";
import * as Sentry from "@sentry/react";


const sentryDsn = process.env.REACT_SENTRY_API;
if (sentryDsn !== undefined) {
    Sentry.init({
        dsn: sentryDsn,
        integrations: [
            new Sentry.BrowserTracing({
                // See docs for support of different versions of variation of react router
                // https://docs.sentry.io/platforms/javascript/guides/react/configuration/integrations/react-router/
                routingInstrumentation: Sentry.reactRouterV6Instrumentation(
                    React.useEffect,
                    useLocation,
                    useNavigationType,
                    createRoutesFromChildren,
                    matchRoutes
                ),
            }),
            new Sentry.Replay()
        ],

        // Set tracesSampleRate to 1.0 to capture 100%
        // of transactions for performance monitoring.
        tracesSampleRate: 1.0,

        // Set `tracePropagationTargets` to control for which URLs distributed tracing should be enabled
        //tracePropagationTargets: ["localhost", /^https:\/\/yourserver\.io\/api/],

        // Capture Replay for 10% of all sessions,
        // plus for 100% of sessions with an error
        replaysSessionSampleRate: 0.1,
        replaysOnErrorSampleRate: 1.0,
    });
}

const Home = lazy(() => import('./Home'));
const Import = lazy(() => import('./import/Import'));
const ImdbImport = lazy(() => import('./import/ImdbImport'));
const TraktImport = lazy(() => import('./import/TraktImport'));
const MyMoviesMap = lazy(() => import('./movies/MyMoviesMap'));
const MovieDetails = lazy(() => import('./movies/MovieDetails'));
const CountryPage = lazy(() => import('./movies/CountryPage'));
const Admin = lazy(() => import('./admin/Admin'));


const stores = {
    movieStore: new MovieStore()
}

const wrapInSuspense = (component: any) => {
    return <Suspense fallback={<div>Loading...</div>}>
        {component}
    </Suspense>
}

const SentryRoutes = Sentry.withSentryReactRouterV6Routing(Routes);

const Main = () => {
    return (
        <div>
            <Header/>
            <Provider {...stores}>
                <BrowserRouter>
                    <SentryRoutes>
                        <Route path="/" index element={wrapInSuspense(<Home/>)}/>
                        <Route path="/map" element={wrapInSuspense(<MyMoviesMap/>)}/>
                        <Route path="/import" element={wrapInSuspense(<Import/>)}/>
                        <Route path="/import/trakt" element={wrapInSuspense(<TraktImport/>)}/>
                        <Route path="/import/imdb" element={wrapInSuspense(<ImdbImport/>)}/>
                        <Route path="/admin" element={wrapInSuspense(<Admin/>)}/>
                        <Route path="/country/:countryCode" element={wrapInSuspense(<CountryPage/>)}/>
                        <Route path="/movie/:movieId" element={wrapInSuspense(<MovieDetails/>)}/>
                    </SentryRoutes>
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
