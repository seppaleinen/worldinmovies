import React, {MutableRefObject} from 'react';
import './MyMoviesMap.scss';
import {VectorMap} from "@react-jvectormap/core"
import axios, {AxiosResponse} from 'axios';
import {inject, observer} from "mobx-react";
import {MyMovie, MyMovieMapState, Props} from "../Types";
import {IMapObject} from "@react-jvectormap/core/dist/types";
import customWorldMapJson from './customworldmap.json';

@inject('movieStore', 'stateStore')
@observer
class MyMoviesMap extends React.Component<Props, MyMovieMapState> {
    myRef = React.createRef<MutableRefObject<IMapObject> & null>();

    constructor(props: Props) {
        super(props);
        this.state = {
            myMovies: {},
        };
        props.movieStore!.hydrateStore().then(() => {
            this.setState({
                myMovies: props.movieStore!.myMovies
            })
            this.generateColors();
        });
    }

    generateColors = () => {
        let colors: Record<string, string> = {}, key;

        // @ts-ignore
        for (key in this.myRef.current.getMapObject().regions) {
            const found = this.state.myMovies !== undefined && key in this.state.myMovies;
            colors[key] = (found ? 'seen' /* light green */ : 'unseen' /* gray */);
        }
        return colors;
    };

    componentDidUpdate(prevProps: Props, prevState: MyMovieMapState) {
        if (this.state.myMovies !== prevState.myMovies) {
            // @ts-ignore
            this.myRef.current.getMapObject().series.regions[0].setValues(this.generateColors());
        }
    }

    onRegionClick = (event: any, code: string) => {
        // @ts-ignore
        const regionName = this.myRef.current.getMapObject().getRegionName(code);
        axios.get("/backend/view/best/" + code.toUpperCase(), {timeout: 5000})
            .then((response: AxiosResponse) => {
                let movieStore = this.props.movieStore!;
                let stateStore = this.props.stateStore!;
                movieStore.movies = response.data.result;
                stateStore.code = code;
                stateStore.regionName = regionName;
                this.props.redirectToPage("movie-details");
            })
            .catch(function (error: any) {
                console.log(error);
            })
            .finally(() => {
                // @ts-ignore
                this.myRef.current.getMapObject().tip.hide();
            });

    };

    changeDataStateCallback = (data: Record<string, MyMovie[]>) => {
        this.setState({
            myMovies: data
        })
    }

    componentDidMount() {
        this.generateColors();
    }

    render() {
        return (
            <div className="map-container inner-map-container">
                <div id="mappy">
                    <VectorMap
                        map={customWorldMapJson}
                        backgroundColor="var(--dominant)"
                        mapRef={this.myRef}
                        series={{
                            regions:
                                [{
                                    attribute: 'fill',
                                    scale: {seen: 'var(--accent)', unseen: 'var(--complement)'},
                                }]
                        }}
                        regionStyle={{
                            hover: {fill: '#A8D4FF'},
                            initial: {fill: 'var(--complement)'}
                        }}
                        zoomOnScroll={false}
                        onRegionClick={this.onRegionClick}
                        className="map"
                    />
                </div>
            </div>
        );
    }
}

export default MyMoviesMap;