import React from 'react';
import './Home.scss';
import MyMoviesMap from './movies/MyMoviesMap';
import Header from "./Header";

const Home = () => {
    return (
        <div>
            <Header/>

            <MyMoviesMap/>
        </div>
    )
}

export default Home;