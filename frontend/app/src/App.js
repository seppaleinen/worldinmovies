import React from 'react';
import './App.css';
import Welcome from './Welcome';
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

        <Welcome/>
        <MyMoviesMap data={this.state.data}/>
      </div>
    )
  }
}

export default Header;
