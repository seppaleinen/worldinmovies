import React from 'react';
import './App.css';
import MyMoviesMap from './MyMoviesMap';
import {inject, observer} from "mobx-react";
import {Props} from "./Types";

@inject('store')
@observer
class App extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
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

                <MyMoviesMap data={this.props.data}/>
            </div>
        )
    }
}

export default App;
