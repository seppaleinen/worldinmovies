import React from 'react';
import './ImdbImport.scss';
import {inject, observer} from "mobx-react";
import MovieStore, {StoreType} from "../stores/MovieStore";
import {useNavigate} from "react-router-dom";

export interface FileUploadProps {
    movieStore?: StoreType;
}

const ImdbImport = inject("movieStore")(observer(({movieStore}: { movieStore?: MovieStore }) => {
    const backendUrl = process.env.REACT_APP_BACKEND_URL === undefined ? '/backend' : process.env.REACT_APP_BACKEND_URL;
    const navigate = useNavigate();

    const onChangeHandler = (event: any) => {
        document.getElementById("earth")!.style.display = "block";
        const data = new FormData()
        data.append('file', event.target.files[0])
        fetch(`${backendUrl}/ratings`,
            {
                method: 'POST',
                body: data
            })
            .then(resp => resp.json())
            .then(res => {
                movieStore!.importMovies(res.found);
            })
            .catch((error: any) => {
                console.error(error);
            })
            .finally(() => {
                document.getElementById("earth")!.style.display = "none";
                navigate("/map");
            });
    }

    return (
        <div className="import">
            <div className="ads">
                <ul>
                    <li>Login to <a
                        href="https://www.imdb.com/registration/signin?u=https%3A//www.imdb.com/&ref_=nv_generic_lgin"
                        target="_blank" rel="noopener noreferrer">imdb</a></li>
                    <li>Click on your username on the upper right corner</li>
                    <li>Click on <b>'Your Ratings'</b></li>
                    <li>Click on the three dots just under <b>'Watchlist'</b></li>
                    <li>Click <b>Export</b></li>
                    <li>Click on Import IMDB data button here and choose the file that you've downloaded</li>
                </ul>
            </div>

            <div className="upload-btn-wrapper">
                <button className="btn btn-success btn-block">Import IMDB data</button>
                <input type="file" name="file" onChange={onChangeHandler}/>
            </div>

            <div id="earth"></div>
        </div>
    )
}));

export default ImdbImport;
