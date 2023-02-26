import React, {useEffect} from 'react';
import './Home.scss';
import Header from "./Header";
import Welcome from "./Welcome";
import MyMoviesMap from "./movies/MyMoviesMap";
import Import from "./import/Import";
import TraktImport from "./import/TraktImport";
import ImdbImport from "./import/ImdbImport";
import MovieModal from "./movies/MovieModal";

const Home = () => {
    const [pageContent, setPageContent] = React.useState('welcome')

    useEffect(() => {
        console.log("Pagecontent: " + pageContent);
    }, [pageContent]);

    const changePage = (page: string) => {
        setPageContent(page);
    }

    return (
        <div>
            <Header redirectToPage={changePage}/>

            {pageContent === 'welcome' ? <Welcome/> : null}
            {pageContent === 'worldmap' ? <MyMoviesMap redirectToPage={changePage}/> : null}
            {pageContent === 'import' ? <Import redirectToPage={changePage}/> : null}
            {pageContent === 'trakt' ? <TraktImport/> : null}
            {pageContent === 'imdb' ? <ImdbImport redirectToPage={changePage}/> : null}
            {pageContent === 'movie-details' ? <MovieModal/> : null}
        </div>
    )
}

export default Home;