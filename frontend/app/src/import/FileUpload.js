import React from 'react';
import axios from 'axios';
import './FileUpload.css';
import { inject, observer } from "mobx-react";

var FileUpload = inject("store")(
  observer(
    class FileUpload extends React.Component {
      onChangeHandler = (event) => {
        document.getElementById("earth").style.display = "block";
        const data = new FormData()
        data.append('file', event.target.files[0])
        axios.post(process.env.REACT_APP_BACKEND_URL + "/ratings", data, {})
          .then(res => { // then print response status
            this.props.changeDataStateCallback(res.data);
            this.props.store.myMovies = res.data.found;
          })
          .finally(() => {
            document.getElementById("earth").style.display = "none";
            this.props.store.closeImportModal();
          });
      }


      render() {
        return (
            <div className="import">
                <div className="ads">
                  <ul>
                    <li>Login to <a href="https://www.imdb.com/registration/signin?u=https%3A//www.imdb.com/&ref_=nv_generic_lgin" target="_blank" rel="noopener noreferrer">imdb</a></li>
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
  )
)

export default FileUpload;
