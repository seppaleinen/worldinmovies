import React from 'react';
import './App.css';
import FileUpload from './FileUpload';

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

        <FileUpload/>
      </div>
  )
}

export default Header;
