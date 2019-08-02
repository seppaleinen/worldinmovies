import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import { VectorMap } from "react-jvectormap"
import axios from 'axios';

function Index() {
  return (
    <div>
      Welcome!
    </div>
  )
}

class Map extends React.Component {
    onRegionTipShow = (event, element, code) => {
        element.html('View top ranked movies from ' + element.html());
    };

    onRegionClick = (event, code) => {
        const regionName = this.refs.map.getMapObject().getRegionName(code);
        axios.get(process.env.REACT_APP_BACKEND_URL + "/view/best/" + code.toUpperCase())
            .then((response) => {
                var tableRowsHtml = response.data
                    .map(item => '<tr>' +
                        '<td style="padding: 5px 15px 5px 10px;"></td>' +
                        '<td style="padding: 5px 15px 5px 10px;"><a style="font-family: Helvetica; text-decoration: none; " href="https://www.imdb.com/title/' + item['imdb_id'] + '">' + item['original_title'] + '</a></td>' +
                        '<td style="padding: 5px 15px 5px 10px;">' + item['vote_average'] + '</td>' +
                        '</tr>').join('')
                var html = '<h2>Top ranked movies from ' + regionName + '</h2>'
                    + '<table class="superduper">'
                    + '<tr><th style="text-align:left; padding: 5px 15px 5px 10px;">#</th><th style="text-align:left; padding: 5px 15px 5px 10px;">Title</th><th style="padding: 5px 15px 5px 10px;">Rating</th></tr>'
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
            <div>
                <div id="mappy" style={{width: '80%', height: 500}}>
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

function Next() {
  return (
    <h2>hej</h2>
  )
}

function Header() {
  return (
    <Router>
      <div>
        <nav>
          <ul>
            <li>
              <Link to="/">Home</Link>
            </li>
            <li>
              <Link to="/map/">Map</Link>
            </li>
            <li>
              <Link to="/next/">Next</Link>
            </li>
          </ul>
        </nav>

        <Route path="/" exact component={Index} />
        <Route path="/map" exact component={Map} />
        <Route path="/next/" component={Next} />
      </div>
    </Router>
  )
}

export default Header;
