import React, {useEffect, useState} from 'react';
import styles from './Admin.module.scss';
// @ts-ignore
import ndjsonStream from "can-ndjson-stream";

const baseUrl = process.env.REACT_APP_BACKEND_URL === undefined ? '/backend' : process.env.REACT_APP_BACKEND_URL;
const tmdbUrl = process.env.REACT_APP_TMDB_URL === undefined ? '/tmdb' : process.env.REACT_APP_TMDB_URL;
const ws_scheme = window.location.protocol === "https:" ? "wss" : "ws";
const websocketUrl = ws_scheme + '://' + window.location.host

const Admin = () => {
    const [status, setStatus] = useState({"fetched": 0, "total": 0, "percentageDone": 0});
    const [baseImport, setBaseImport] = useState<string[]>([]);
    const [toggle, setToggle] = useState<string>("tmdb")

    useEffect(() => {
        let backend = null;
        switch (toggle) {
            case 'tmdb':
                backend = tmdbUrl;
                break;
            case 'base':
                backend = baseUrl;
                break;
        }
        fetch(`${backend}/status`)
            .then(response => response.json())
            .then(response => {
                setStatus(response);
            })
            .catch(error => console.error(error))
        const ws = new WebSocket(`${websocketUrl}/${backend}/ws`);
        ws.onmessage = (event) => {
            setBaseImport(prevState =>  [...prevState, event.data])
        }
        ws.onerror = (error) => {
            console.log(error)
        }
    }, [toggle]);

    const triggerImport = (path: string) => {
        fetch(path)
            .then((response: Response) => ndjsonStream(response.body))
            .then((stream: ReadableStream<string>) => {
                const reader = stream.getReader();
                let read: any;
                reader.read().then(read = (result: ReadableStreamReadResult<string>) => {
                    if (result.done) {
                        return;
                    }

                    reader.read().then(read);
                });
            })
    }

    const handleClick = (newState: string) => {
        setToggle(newState)
    }

    return (
        <div className={styles.container}>
            <div className={styles.toggle}>
                <h2 onClick={() => handleClick('tmdb')}
                    className={toggle === 'tmdb' ? styles.activeToggle : styles.inactiveToggle}>TMDB</h2>
                <h2 onClick={() => handleClick('base')}
                    className={toggle === 'base' ? styles.activeToggle : styles.inactiveToggle}>Base</h2>
            </div>
            <span className={styles.status}>Fetched {status.fetched} out of {status.total} movies which is {status.percentageDone}%</span><br/>

            <div className={styles.buttons}>
                <div className={toggle === 'tmdb' ? styles.show : styles.hide}>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/base')}>Import TMDB Base
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/data')}>Import TMDB Data
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/languages')}>Import TMDB
                        Languages
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/genres')}>Import TMDB
                        Genres
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/countries')}>Import TMDB
                        Countries
                    </button>
                    <button className="button" onClick={() => triggerImport(tmdbUrl + '/import/tmdb/changes')}>Import TMDB
                        Changes
                    </button>
                </div>
                <div className={toggle === 'base' ? styles.show : styles.hide}>
                    <button className="button" onClick={() => triggerImport(baseUrl + '/import/imdb/ratings')}>Import IMDB
                        Ratings
                    </button>
                    <button className="button" onClick={() => triggerImport(baseUrl + '/import/imdb/titles')}>Import IMDB
                        Titles
                    </button>
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
