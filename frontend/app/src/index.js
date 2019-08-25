import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import { Provider } from "mobx-react";
import Store from "./MobxStore";
import * as serviceWorker from './serviceWorker';
import * as Sentry from '@sentry/browser';

const sentryUrl = process.env.REACT_APP_SENTRY_URL;
if(sentryUrl !== undefined && sentryUrl.length !== 0) {
  Sentry.init({dsn: sentryUrl});
}

const stores  = {
  store: new Store()
}

const Main = () =>
  <Provider {...stores}>
    <App />
  </Provider>

ReactDOM.render(<Main />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
