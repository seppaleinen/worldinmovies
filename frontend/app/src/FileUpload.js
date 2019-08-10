import React from 'react';
import axios from 'axios';
import MyMoviesMap from './MyMoviesMap';
import './FileUpload.css';

class FileUpload extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedFile: null,
            data: null
        }
    }

    onChangeHandler=event=>{
        this.setState({
            selectedFile: event.target.files[0],
            loaded: 0,
        })
    }
    onClickHandler = () => {
        document.getElementById("loader").style.display = "block";
        const data = new FormData()
        data.append('file', this.state.selectedFile)
        axios.post(process.env.REACT_APP_BACKEND_URL + "/ratings", data, {})
            .then(res => { // then print response status
                document.getElementById("loader").style.display = "none";
                console.log(res.statusText);
                console.log(res.data);
                this.movies = res.data;
                this.setState(state => ({
                    data: res.data
                }));
            })
            .catch(function (error) {
                console.log(error);
            });

    }
    render() {
        return (
            <div className="main">
                Helloooo
                <br/>
                <input type="file" name="file" onChange={this.onChangeHandler}/>
                <button type="button" className="btn btn-success btn-block" onClick={this.onClickHandler}>Upload</button>
                <MyMoviesMap data={this.state.data}/>
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

export default FileUpload;
