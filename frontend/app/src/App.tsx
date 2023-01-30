import React from 'react';
import './App.css';
import MyMoviesMap from './MyMoviesMap';
import {inject, observer} from "mobx-react";
import {Props} from "./Types";
import {Header} from "./Header";

@inject('store')
@observer
class App extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }

    render() {
        return (
            <div>
                <Header/>

                <MyMoviesMap data={this.props.data}/>
            </div>
        )
    }
}

export default App;
