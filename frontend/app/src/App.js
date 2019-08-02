import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import { VectorMap } from "react-jvectormap"

function Index() {
  return (
    <div>
      Welcome!
    </div>
  )
}

const onRegionClick = (event, code) => {
    var html = '<h2>Top ranked movies from ' + code + '</h2>';
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

const onRegionTipShow = (event, element, code) => {
    element.html('View top ranked movies from ' + element.html());
};

class Map extends React.Component {
    render() {
        return (
            <div>
                <div style={{width: '80%', height: 500}}>
                    <VectorMap map={'world_mill'}
                            backgroundColor="#a5bfdd"
                            ref="map"
                            containerStyle={{
                                width: '100%',
                                height: '100%'
                            }}
                            regionStyle={{hover: {fill: '#c9dfaf'}}}
                            onRegionClick={onRegionClick}
                            onRegionTipShow={onRegionTipShow}
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
