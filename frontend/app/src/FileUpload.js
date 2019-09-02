import React from 'react';
import axios from 'axios';
import './FileUpload.css';
import { inject, observer } from "mobx-react";

var FileUpload = inject("store")(
  observer(
    class FileUpload extends React.Component {
      onChangeHandler = (event) => {
        document.getElementById("loader").style.display = "block";
        const data = new FormData()
        data.append('file', event.target.files[0])
        axios.post(process.env.REACT_APP_BACKEND_URL + "/ratings", data, {})
          .then(res => { // then print response status
            this.props.changeDataStateCallback(res.data);
            this.props.store.myMovies = res.data.found;
          })
          .catch(function (error) {
            console.log(error);
          })
          .finally(function () {
            document.getElementById("loader").style.display = "none";
          });
      }


      render() {
        return (
            <div className="import">
                <div className="upload-btn-wrapper">
                  <button className="btn btn-success btn-block">Import IMDB data</button>
                  <input type="file" name="file" onChange={this.onChangeHandler}/>
                </div>

                <div id="loader">
                    <div className="sk-circle">
                        <div className="sk-circle1 sk-child"></div>
                        <div className="sk-circle2 sk-child"></div>
                        <div className="sk-circle3 sk-child"></div>
                        <div className="sk-circle4 sk-child"></div>
                        <div className="sk-circle5 sk-child"></div>
                        <div className="sk-circle6 sk-child"></div>
                        <div className="sk-circle7 sk-child"></div>
                        <div className="sk-circle8 sk-child"></div>
                        <div className="sk-circle9 sk-child"></div>
                        <div className="sk-circle10 sk-child"></div>
                        <div className="sk-circle11 sk-child"></div>
                        <div className="sk-circle12 sk-child"></div>
                    </div>
                </div>
            </div>
        )
      }
    }
  )
)

export default FileUpload;
