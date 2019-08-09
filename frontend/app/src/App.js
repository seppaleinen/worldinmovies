import React from 'react';
import './App.css';
import FileUpload from './FileUpload';
import Welcome from './Welcome';

function Header() {
  return (
      <div>
        <nav>
          <ul>
            <li>
              <a href="/">Home</a>
            </li>
          </ul>
        </nav>

        <Welcome/>
        <FileUpload/>
      </div>
  )
}

export default Header;
