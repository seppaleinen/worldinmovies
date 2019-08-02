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
const onRegionOver = (event, code) => {
    console.log(`You're hovering ${code}`)
};

const onRegionClick = (event, code) => {
    console.log(`You have clicked ${code}`)
};


class Map extends React.Component {
    render() {
        return (
            <div style={{width: 500, height: 500}}>
                <VectorMap map={'world_mill'}
                           backgroundColor="#a5bfdd"
                           ref="map"
                           containerStyle={{
                               width: '100%',
                               height: '100%'
                           }}
                           onRegionOver={onRegionOver}
                           onRegionClick={onRegionClick}
                           containerClassName="map"
                />
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
