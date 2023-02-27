import React, {useState, useEffect} from 'react';
import './Admin.scss';
import axios from "axios";
// @ts-ignore
import ndjsonStream from "can-ndjson-stream";
import Header from "../Header";

const Admin = () => {
    const [status, setStatus] = useState({"fetched": 0, "total": 0, "percentage_done": 0});
    const [baseImport, setBaseImport] = useState<string[]>([]);

    useEffect(() => {
        fetch('/backend/status')
            .then(response => response.json())
            .then(response => {
                setStatus(response);
            })
    }, []);

    const startLanguageImport = (path: string) => {
        fetch("/backend" + path)
            .then((response: Response) => ndjsonStream(response.body))
            .then((stream: ReadableStream<string>) => {
                const reader = stream.getReader();
                let read: any;
                reader.read().then(read = (result: ReadableStreamReadResult<string>) => {
                    if (result.done) {
                        return;
                    }

                    setBaseImport(prevState => [...prevState.slice(-9), result.value])
                    reader.read().then(read);
                });
            })
    }

    return (
        <div>
            <Header redirectToPage={() => console.log("")}/>

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
