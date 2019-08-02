import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";

function Index() {
  return (
    <div>
      Welcome!
    </div>
  )
}

function Map() {
  return (
        <div>
            <div id="vmap" style={{width: '600px', height: '400px'}}></div>

            <div id="myModal" className="modal">
                <div className="modal-content">
                    <span className='close'>&times;</span>
                    <div id="modal-text"></div>
                </div>
            </div>
        </div>
  );
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
