import React from 'react';
import './MyMoviesMap.css';
import FileUpload from './FileUpload';
import MovieModal from './MovieModal';
import { VectorMap } from "react-jvectormap"
import axios from 'axios';
import { inject, observer } from "mobx-react";

var MyMoviesMap = inject("store")(
  observer(
  class MyMoviesMap extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: props.data,
            importModalShow: false,
            rerenderModal: Math.random()
        }
    }

    is_movie_from_country(movie, code) {
        return movie.country_codes.find(country_code => {return code === country_code;});
    }

    generateColors = () => {
        var colors = {}, key;

        for (key in this.refs.map.getMapObject().regions) {
            var found = this.state.data.found_responses.find(movie => this.is_movie_from_country(movie, key));
            var color = (found ? 'seen' /* light green */ : 'unseen' /* gray */);
            colors[key] = color;
        }
        return colors;
    };

    componentDidUpdate(prevProps, prevState) {
        if(this.state.data !== prevState.data) {
            this.refs.map.getMapObject().series.regions[0].setValues(this.generateColors());
        }
        if(this.state.rerenderModal !== prevState.rerenderModal) {
          this.setState({rerenderModal: Math.random()});
        }
    }

    onRegionClick = (event, code) => {
        const regionName = this.refs.map.getMapObject().getRegionName(code);
        this.props.store.showMovieModal = true;
        axios.get(process.env.REACT_APP_BACKEND_URL + "/view/lang/best/" + code.toUpperCase())
                    .then((response) => {
                      this.props.store.movies = response.data;
                      this.props.store.code = code;
                      this.props.store.regionName = regionName;
                      this.setState({rerenderModal: Math.random()});
                    })
                    .catch(function (error) {
                        console.log(error);
                    });

    };

    changeDataStateCallback = (data) => {
      this.setState({
        data: data
      })
    }

    showModal = () => {
      this.setState({
        importModalShow: true
      })
    }
    hideModal = () => {
      this.setState({
        importModalShow: false
      })
    }

    render() {
        return (
            <div className="map-container inner-map-container">
                <FileUpload changeDataStateCallback={this.changeDataStateCallback}/>
                <div id="mappy">
                    <VectorMap
                            map={'world_mill'}
                            backgroundColor="#a5bfdd"
                            ref="map"
                            containerStyle={{
                                width: '100%',
                                height: '100%'
                            }}
                            series={{'regions':
                              [{
                                'attribute': 'fill',
                                scale: {seen: '#c9dfaf', unseen: '#A8A8A8'},
                                legend: {vertical: true, title: 'Country colors'}
                              }]
                            }}
                            regionStyle={{
                              hover: {fill: '#c9dfaf'},
                              initial: {fill: '#c9dfaf'}
                            }}
                            zoomOnScroll={false}
                            onRegionClick={this.onRegionClick}
                            containerClassName="map"
                    />

                    <MovieModal rerender={this.state.rerenderModal}/>
                </div>
            </div>
        );
    }
}))

export default MyMoviesMap;