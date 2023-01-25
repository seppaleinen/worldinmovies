import React, {useState, useEffect} from 'react';
import './Admin.css';
import axios from "axios";
import {fromFetch} from "rxjs/fetch";
import {concatMap} from "rxjs/operators";

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
const Admin = (props: any) => {
    const [status, setStatus] = useState({"fetched": 0, "total": 0, "percentage_done": 0});
    const [baseImport, setBaseImport] = useState([]);

    useEffect(() => {
        async function getStatus() {
            let response = await axios.get(process.env.REACT_APP_BACKEND_URL + '/status', {timeout: 5000});
            setStatus(response.data);
        }

        getStatus();
    }, []);

    const startLanguageImport = (path) => {
        const sub$ = fromFetch(process.env.REACT_APP_BACKEND_URL + path,
            {
                selector: response => {
                    return response.text();
                }
            })
            .pipe(
                concatMap(data => JSON.parse("[" + data.toString().slice(0, -1) + "]"))
            )
            .subscribe({
                next: value => {
                    // Add latest response and remove all but previous 9 entries
                    setBaseImport(prevState => [...prevState.slice(-9), value]);
                },
                complete: () => {
                    console.log("Done");
                },
                error: err => console.error("Error: " + err)
            });
        return () => sub$.unsubscribe();
    }

    return (
        <div>
            <nav>
                <ul>
                    <li>
                        <a href="/">Home</a>
                    </li>
                </ul>
            </nav>

            <span>Fetched {status.fetched} out of {status.total} movies which is {status.percentage_done}%</span><br/>

            <button onClick={() => startLanguageImport('/import/tmdb/languages')}>Start Language Import</button>
            <br/>
            <button onClick={() => startLanguageImport('/stream')}>Stream test</button>
            <br/>
            <button onClick={() => startLanguageImport('/import/tmdb/data')}>Start Data Import</button>
            <br/>
            <div id="container">
                <div id="content">
                    Log
                    {baseImport.map((object, i) => <p key={i}>{JSON.stringify(object)}</p>)}
                </div>
            </div>
        </div>
    )

}

export default Admin;
