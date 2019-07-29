import React from 'react';
import './App.css';

function App() {
  return (
        <div>
            <div id="vmap" style={{width: '600px', height: '400px'}}></div>

            <div id="myModal" className="modal">
                <div className="modal-content">
                    <span className="close">&times;</span>
                    <div id="modal-text"></div>
                </div>
            </div>
        </div>
  );
}

export default App;
