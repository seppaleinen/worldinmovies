import React from 'react';
import './App.css';
import { VectorMap } from "react-jvectormap"
import axios from 'axios';

class MyMoviesMap extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: props.data
        }
    }

    onRegionTipShow = (event, element, code) => {
        element.html("View which movies you've seen from " + element.html());
    };

    generateColors = () => {
        var colors = {}, key;
        console.log("asd");
        for (key in this.refs.map.getMapObject().regions) {
            var found = this.props.data.found_responses.find(movie => {return movie.country_codes.find(country_code => {return key === country_code;});})
            var color = (found ? 'green' : 'red');
            colors[key] = color;
        }
        return colors;
    };

    componentDidUpdate(prevProps, prevState, snapshot) {
        if(this.props.data !== prevProps.data) {
            this.refs.map.getMapObject().series.regions[0].setValues(this.generateColors());
        }
    }

    onRegionClick = (event, code) => {
        var html;
        if (this.props.data === undefined || this.props.data === null || this.props.data.length === 0) {
            html = '<h2>No movies fetched yet</h2>';
        } else {
            const regionName = this.refs.map.getMapObject().getRegionName(code);
            let rows = this.props.data.found_responses
                .filter(movie => {
                    return movie.country_codes.find(country_code => {return country_code === code;});
                })
                .sort((a, b) => (a.personal_rating > b.personal_rating) ? -1 : 1)
                .map(item => '<tr>' +
                            '<td></td>' +
                            '<td><a href="https://www.imdb.com/title/' + item['imdb_id'] + '">' + item['title'] + '</a></td>' +
                            '<td>' + item['personal_rating'] + '</td>' +
                            '</tr>')
                .join('');
            if (rows.length === 0) {
                axios.get(process.env.REACT_APP_BACKEND_URL + "/view/lang/best/" + code.toUpperCase())
                    .then((response) => {
                        var tableRowsHtml = response.data
                            .map(item => '<tr>' +
                                '<td></td>' +
                                '<td><a href="https://www.imdb.com/title/' + item['imdb_id'] + '">' + item['original_title'] + '</a></td>' +
                                '<td>' + item['vote_average'] + '</td>' +
                                '</tr>').join('')
                        var html = "<h2>You haven't seen anything from " + regionName + ' yet, here are some recommendations</h2>'
                        + '<table class="modal-table">'
                        + '<tr>'
                        + '<th>#</th>'
                        + '<th>Title</th>'
                        + '<th>Rating</th></tr>'
                        + tableRowsHtml
                        + '</table>';

                        var modal = document.getElementById("myModal");
                        var modalText = document.getElementById("modal-text");
                        modalText.innerHTML = html;
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
            } else {
            html = '<h2>Top ranked movies from ' + regionName + '</h2>'
                        + '<table class="modal-table">'
                        + '<tr>'
                        + '<th>#</th>'
                        + '<th>Title</th>'
                        + '<th>Rating</th></tr>'
                        + rows
                        + '</table>';
            }
        }

        var modal = document.getElementById("myModal");
        var modalText = document.getElementById("modal-text");
        modalText.innerHTML = html;
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
    };

    render() {
        return (
            <div className="main">
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
                            regionStyle={{hover: {fill: '#c9dfaf'}}}
                            onRegionClick={this.onRegionClick}
                            onRegionTipShow={this.onRegionTipShow2}
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