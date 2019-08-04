import React from 'react';
import axios from 'axios';
import MyMoviesMap from './MyMoviesMap';

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
        const data = new FormData()
        data.append('file', this.state.selectedFile)
        axios.post(process.env.REACT_APP_BACKEND_URL + "/ratings", data, {})
            .then(res => { // then print response status
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
            </div>
        )
    }
}

export default FileUpload;
