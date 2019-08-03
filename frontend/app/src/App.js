import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import ProductionCountryMap from './ProductionCountryMap';
import LanguageBasedMap from './LanguageBasedMap';
import FileUpload from './FileUpload';

class Index extends React.Component {
    render() {
        return (
            <div className="main">
                Welcome!
            </div>
        )
    }
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
              <Link to="/map/production-country">Map</Link>
            </li>
            <li>
              <Link to="/map/language">Map by language</Link>
            </li>
            <li>
              <Link to="/file">Upload file</Link>
            </li>
          </ul>
        </nav>

        <Route path="/" exact component={Index} />
        <Route path="/map/production-country" exact component={ProductionCountryMap} />
        <Route path="/map/language" exact component={LanguageBasedMap} />
        <Route path="/file" exact component={FileUpload} />
      </div>
    </Router>
  )
}

export default Header;
