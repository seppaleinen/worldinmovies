import React from 'react';
import './App.css';
import MyMoviesMap from './MyMoviesMap';
import {Header} from "./Header";

const App = () => {
    return (
        <div>
            <Header/>

            <MyMoviesMap/>
        </div>
    )
}

export default App;