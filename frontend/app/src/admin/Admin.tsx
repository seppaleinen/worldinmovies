import React, {useState, useEffect} from 'react';
import './Admin.css';
import axios from "axios";
// @ts-ignore
import ndjsonStream from "can-ndjson-stream";
import Header from "../Header";

/**
 *     # Imports a daily file with the data of what movies are available to download
 *     path('import/tmdb/daily',               views.download_file),
 *     # Starts to fetch info from tmdb with the keys from daily
 *     path('import/tmdb/data',                views.fetch_movie),
 *     # Runs /daily, /genres, /countries, /languages
 *     path('import/base',                     views.base_fetch),
 *     path('import/tmdb/genres',              views.fetch_genres),
 *     path('import/tmdb/countries',           views.fetch_countries),
 *     path('import/tmdb/languages',           views.fetch_languages),
 *     path('import/tmdb/changes',             views.check_tmdb_for_changes),
 *     path('import/imdb/ratings',             views.fetch_imdb_ratings),
 *     path('import/imdb/titles',              views.fetch_imdb_titles),
 *     re_path(r'^status$',                    views.import_status, name='import_status'),
 */
const Admin = () => {
    const [status, setStatus] = useState({"fetched": 0, "total": 0, "percentage_done": 0});
    const [baseImport, setBaseImport] = useState<string[]>([]);

    useEffect(() => {
        async function getStatus() {
            let response = await axios.get('/backend/status', {timeout: 5000});
            setStatus(response.data);
        }

        getStatus();
    }, []);

    const startLanguageImport = (path: string) => {
        fetch("/backend" + path)
            .then((response: Response) => ndjsonStream( response.body ))
            .then((stream: ReadableStream<string>) => {
                const reader = stream.getReader();
                let read: any;
                reader.read().then( read = ( result: ReadableStreamReadResult<string> ) => {
                    if ( result.done ) {
                        return;
                    }

                    setBaseImport(prevState => [...prevState.slice(-9), result.value])
                    reader.read().then( read );
                } );
            })
    }

    return (
        <div>
            <Header/>

            <span>Fetched {status.fetched} out of {status.total} movies which is {status.percentage_done}%</span><br/>

            <button onClick={() => startLanguageImport('/import/base')}>Import TMDB Base</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/tmdb/data')}>Import TMDB Data</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/tmdb/languages')}>Import TMDB Languages</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/tmdb/genres')}>Import TMDB Genres</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/tmdb/countries')}>Import TMDB Countries</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/tmdb/changes')}>Import TMDB Changes</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/imdb/ratings')}>Import IMDB Ratings</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/imdb/titles')}>Import IMDB Titles</button>
            <br/>
            <div id="container">
                <div id="content">
                    Log
                    {baseImport.map((line, index) => <p key={index}>{JSON.stringify(line)}</p>)}
                </div>
            </div>
        </div>
    )

}

export default Admin;
