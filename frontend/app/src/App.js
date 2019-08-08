import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import ProductionCountryMap from './ProductionCountryMap';
import LanguageBasedMap from './LanguageBasedMap';
import MyMoviesMap from './MyMoviesMap';
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
              <a href="/">Home</a>
            </li>
          </ul>
        </nav>

        <FileUpload/>
      </div>
    </Router>
  )
}

export default Header;
