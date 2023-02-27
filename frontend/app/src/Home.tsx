import React, {lazy, Suspense} from 'react';
import './Home.scss';
import Header from "./Header";
import Welcome from "./Welcome";
import Admin from "./admin/Admin";

const Import = lazy(() => import('./import/Import'));
const ImdbImport = lazy(() => import('./import/ImdbImport'));
const TraktImport = lazy(() => import('./import/TraktImport'));
const MyMoviesMap = lazy(() => import('./movies/MyMoviesMap'));
const MovieModal = lazy(() => import('./movies/MovieModal'));

const Home = (props: Props) => {
    const [pageContent, setPageContent] = React.useState(props.startPage)

    const changePage = (page: string) => {
        setPageContent(page);
    }

    const wrapInSuspense = (component: any) => {
        return <Suspense fallback={<div>Loading...</div>}>
            {component}
        </Suspense>
    }

    return (
        <div>
            <Header redirectToPage={changePage}/>

            {pageContent === 'welcome' ? <Welcome/> : null}
            {pageContent === 'admin' ? wrapInSuspense(<Admin/>) : null}
            {pageContent === 'worldmap' ? wrapInSuspense(<MyMoviesMap redirectToPage={changePage}/>) : null}
            {pageContent === 'import' ? wrapInSuspense(<Import redirectToPage={changePage}/>) : null}
            {pageContent === 'trakt' ? wrapInSuspense(<TraktImport/>) : null}
            {pageContent === 'imdb' ? wrapInSuspense(<ImdbImport redirectToPage={changePage}/>) : null}
            {pageContent === 'movie-details' ? wrapInSuspense(<MovieModal/>) : null}
        </div>
    )
}

interface Props {
    startPage: string;
}

export default Home;