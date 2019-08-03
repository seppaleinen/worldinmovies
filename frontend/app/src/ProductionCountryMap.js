import React from 'react';
import './App.css';
import axios from 'axios';
import { VectorMap } from "react-jvectormap"

class ProductionCountryMap extends React.Component {
    onRegionTipShow = (event, element, code) => {
        element.html('View top ranked movies from ' + element.html());
    };

    onRegionClick = (event, code) => {
        const regionName = this.refs.map.getMapObject().getRegionName(code);
        axios.get(process.env.REACT_APP_BACKEND_URL + "/view/best/" + code.toUpperCase())
            .then((response) => {
                var tableRowsHtml = response.data
                    .map(item => '<tr>' +
                        '<td></td>' +
                        '<td><a href="https://www.imdb.com/title/' + item['imdb_id'] + '">' + item['original_title'] + '</a></td>' +
                        '<td>' + item['vote_average'] + '</td>' +
                        '</tr>').join('')
                var html = '<h2>Top ranked movies from ' + regionName + '</h2>'
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

export default ProductionCountryMap;