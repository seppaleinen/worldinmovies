import React, {useEffect, useState} from 'react';
import styles from './Admin.module.scss';

const baseUrl = process.env.REACT_APP_BACKEND_URL === undefined ? '/backend' : process.env.REACT_APP_BACKEND_URL;
const tmdbUrl = process.env.REACT_APP_TMDB_URL === undefined ? '/tmdb' : process.env.REACT_APP_TMDB_URL;
const neoUrl = process.env.REACT_APP_NEO_URL === undefined ? '/neo' : process.env.REACT_APP_NEO_URL;
const ws_scheme = window.location.protocol === "https:" ? "wss" : "ws";

const Admin = () => {
    const [status, setStatus] = useState({"fetched": 0, "total": 0, "percentageDone": 0});
    const [baseImport, setBaseImport] = useState<string[]>([]);
    const [toggle, setToggle] = useState<string>("tmdb")

    useEffect(() => {
        let backend = "";
        switch (toggle) {
            case 'tmdb':
                backend = tmdbUrl;
                break;
            case 'imdb':
                backend = baseUrl;
                break;
            case 'neo':
                backend = neoUrl;
                break;
        }
        fetch(`${backend}/status`)
            .then(response => response.json())
            .then(response => {
                setStatus(response);
            })
            .catch(error => console.error(error))
        const matcher = backend.match(/.*(:\d+).*/);
        const value = matcher !== null ? matcher[1] : backend;
        const ws = new WebSocket(`${ws_scheme}://${window.location.hostname}${value}/ws`);
        setBaseImport([])
        ws.onmessage = (event) => {
            setBaseImport(prevState => [...prevState, event.data]);
        }
        ws.onerror = (error) => {
            console.log(error)
        }
    }, [toggle]);

    const triggerImport = (path: string) => {
        fetch(path)
            .then(() => setBaseImport(prevState => [...prevState, `${path} called successfully`]))
            .catch(error => setBaseImport(prevState => [...prevState, `Call to ${path} failed due to ${error}`]));
    }

    const handleClick = (newState: string) => {
        setToggle(newState)
    }

    return (
        <div className={styles.container}>
            <div className={styles.toggle}>
                <h2 onClick={() => handleClick('tmdb')}
                    className={toggle === 'tmdb' ? styles.activeToggle : styles.inactiveToggle}>TMDB</h2>
                <h2 onClick={() => handleClick('imdb')}
                    className={toggle === 'imdb' ? styles.activeToggle : styles.inactiveToggle}>IMDB</h2>
                <h2 onClick={() => handleClick('neo')}
                    className={toggle === 'neo' ? styles.activeToggle : styles.inactiveToggle}>Neo4J</h2>
            </div>
            <span
                className={styles.status}>Fetched {status.fetched} out of {status.total} movies which is {status.percentageDone}%</span><br/>

            <div className={styles.buttons}>
                <div className={toggle === 'tmdb' ? styles.show : styles.hide}>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/base')}>Import TMDB Base
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/data')}>Import TMDB
                        Data
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/languages')}>Import
                        TMDB
                        Languages
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/genres')}>Import
                        TMDB
                        Genres
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/countries')}>Import
                        TMDB
                        Countries
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/changes')}>Import
                        TMDB
                        Changes
                    </button>
                </div>
                <div className={toggle === 'imdb' ? styles.show : styles.hide}>
                    <button className="button" onClick={() => triggerImport(baseUrl + '/import/imdb/ratings')}>Import
                        IMDB
                        Ratings
                    </button>
                    <button className="button" onClick={() => triggerImport(baseUrl + '/import/imdb/titles')}>Import
                        IMDB
                        Titles
                    </button>
                </div>
                <div className={toggle === 'neo' ? styles.show : styles.hide}>
                </div>
            </div>
            <div className={styles.terminal}>
                <div className={styles.content}>
                    {baseImport.map((line, index) => <p key={index}>{line}</p>)}
                </div>
            </div>
        </div>
    )

}

export default Admin;
