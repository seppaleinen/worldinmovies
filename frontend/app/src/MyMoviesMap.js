import React from 'react';
import './MyMoviesMap.css';
import FileUpload from './FileUpload';
import { VectorMap } from "react-jvectormap"
import axios from 'axios';

class MyMoviesMap extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: props.data,
            importModalShow: false
        }
    }

    is_movie_from_country(movie, code) {
        return movie.country_codes.find(country_code => {return code === country_code;});
    }

    generateColors = () => {
        var colors = {}, key;

        for (key in this.refs.map.getMapObject().regions) {
            var found = this.state.data.found_responses.find(movie => this.is_movie_from_country(movie, key));
            var color = (found ? '#c9dfaf' /* light green */ : '#A8A8A8' /* gray */);
            colors[key] = color;
        }
        return colors;
    };

    componentDidUpdate(prevProps, prevState) {
        if(this.state.data !== prevState.data) {
            this.refs.map.getMapObject().series.regions[0].setValues(this.generateColors());
        }
    }

    createBestMoviesTable(data, regionName) {
        var tableRowsHtml = data.map(item => '<tr>' +
                                '<td></td>' +
                                '<td><a href="https://www.imdb.com/title/' + item['imdb_id'] + '">' + item['original_title'] + '</a></td>' +
                                '<td>' + item['vote_average'] + '</td>' +
                                '</tr>').join('')
        return '<div id="rankedMoviesTable"><h2>Top ranked movies from ' + regionName + '</h2>'
                        + '<table class="modal-table">'
                        + '<tr>'
                        + '<th>#</th>'
                        + '<th>Title</th>'
                        + '<th>Rating</th></tr>'
                        + tableRowsHtml
                        + '</table></div>';
    }

    createMyMoviesTable(data, code, regionName) {
        let rows = data
                .filter(movie => this.is_movie_from_country(movie, code))
                .sort((a, b) => (a.personal_rating > b.personal_rating) ? -1 : 1)
                .slice(0, 10)
                .map(item => '<tr>' +
                            '<td></td>' +
                            '<td><a href="https://www.imdb.com/title/' + item['imdb_id'] + '">' + item['title'] + '</a></td>' +
                            '<td>' + item['personal_rating'] + '</td>' +
                            '</tr>')
                .join('')
        if(rows.length !== 0) {
          return '<div id="myMoviesTable"><h2>Your top ranked movies from ' + regionName + '</h2>'
                        + '<table class="modal-table">'
                        + '<tr>'
                        + '<th>#</th>'
                        + '<th>Title</th>'
                        + '<th>Your rating</th></tr>'
                        + rows
                        + '</table></div>';
        } else {
          return '';
        }
    }

    onRegionClick = (event, code) => {
        const regionName = this.refs.map.getMapObject().getRegionName(code);
        axios.get(process.env.REACT_APP_BACKEND_URL + "/view/lang/best/" + code.toUpperCase())
                    .then((response) => {
                        var containingSection = '<section id="containingSection">';
                        var html = this.createBestMoviesTable(response.data, regionName);
                        if(this.state.data !== undefined && this.state.data !== null && this.state.data.found_responses.length !== 0) {
                           html += this.createMyMoviesTable(this.state.data.found_responses, code, regionName);
                        }
                        containingSection += html + '</section';

                        var modal = document.getElementById("myModal");
                        var modalText = document.getElementById("modal-text");
                        modalText.innerHTML = containingSection;
                        modal.style.display = "block";

                        // Get the <span> element that closes the modal
                        var span = document.getElementsByClassName("close")[0];

                        // When the user clicks on <span> (x), close the modal
                        span.onclick = function() {
                            modal.style.display = "none";
                        };

                        // When the user clicks anywhere outside of the modal, close it
                        window.onclick = function(event) {
                            if (event.target === modal) {
                                modal.style.display = "none";
                            }
                        }
                    })
                    .catch(function (error) {
                        console.log(error);
                    });

    };

    changeDataStateCallback = (data) => {
      this.setState({
        data: data
      })
    }

    showModal = () => {
      this.setState({
        importModalShow: true
      })
    }
    hideModal = () => {
      this.setState({
        importModalShow: false
      })
    }

    render() {
        return (
            <div className="map-container inner-map-container">
                <FileUpload changeDataStateCallback={this.changeDataStateCallback}/>
                <div id="mappy">
                    <VectorMap
                            map={'world_mill'}
                            backgroundColor="#a5bfdd"
                            ref="map"
                            containerStyle={{
                                width: '100%',
                                height: '100%'
                            }}
                            series={{'regions': [{'attribute': 'fill'}]}}
                            regionStyle={{
                              hover: {fill: '#c9dfaf'},
                              initial: {fill: '#c9dfaf'}
                            }}
                            zoomOnScroll={false}
                            onRegionClick={this.onRegionClick}
                            containerClassName="map"
                    />

                    <div id="myModal" className="modal">
                        <div className="modal-content">
                            <span className='close'>&times;</span>
                            <div id="modal-text"/>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default MyMoviesMap;