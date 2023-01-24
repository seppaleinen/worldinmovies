import React from 'react';
import './App.css';
import MyMoviesMap from './MyMoviesMap';

class Header extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: props.data
    }
  }

  render() {
    return (
      <div>
        <nav>
          <ul>
            <li>
              <a href="/">Home</a>
            </li>
          </ul>
        </nav>

        <MyMoviesMap data={this.state.data}/>
      </div>
    )
  }
}

export default Header;
