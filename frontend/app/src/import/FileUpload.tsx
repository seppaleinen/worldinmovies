import React from 'react';
import axios, {AxiosResponse} from 'axios';
import './FileUpload.scss';
import {inject, observer} from "mobx-react";
import {StoreType} from "../stores/MovieStore";
import {StateStoreType} from "../stores/StateStore";
import {MyMovie, RatingsResponse} from "../Types";

export interface FileUploadProps {
    changeDataStateCallback: (data: Record<string, MyMovie[]>) => void;
    movieStore?: StoreType;
    stateStore?: StateStoreType
}

@inject('movieStore', 'stateStore')
@observer
class FileUpload extends React.Component<FileUploadProps, {}> {
    onChangeHandler = (event: any) => {
        document.getElementById("earth")!.style.display = "block";
        const data = new FormData()
        data.append('file', event.target.files[0])
        axios.post("/backend/ratings", data)
            .then((res: AxiosResponse<RatingsResponse>) => {
                this.props.movieStore!.myMovies = res.data.found;
                this.props.changeDataStateCallback(res.data.found);
            })
            .catch(function (error: any) {
                console.log(error);
            })
            .finally(() => {
                document.getElementById("earth")!.style.display = "none";
                this.props.stateStore!.closeImportModal();
            });
    }

    render() {
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
                    <input type="file" name="file" onChange={this.onChangeHandler}/>
                </div>

                <div id="earth"></div>
            </div>
        )
    }
}

export default FileUpload;
