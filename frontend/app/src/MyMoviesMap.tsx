import React, {MutableRefObject} from 'react';
import './MyMoviesMap.css';
import MovieModal from './MovieModal';
import Import from './import/Import';
import {VectorMap} from "@react-jvectormap/core"
import axios, {AxiosResponse} from 'axios';
import {inject, observer} from "mobx-react";
import {worldMill} from "@react-jvectormap/world";
import {MyMovie, MyMovieMapState, Props} from "./Types";
import {IMapObject} from "@react-jvectormap/core/dist/types";

@inject('movieStore', 'stateStore')
@observer
class MyMoviesMap extends React.Component<Props, MyMovieMapState> {
    myRef = React.createRef<MutableRefObject<IMapObject> & null>();

    constructor(props: Props) {
        super(props);
        this.state = {
            myMovies: {},
            rerenderModal: Math.random(),
            rerenderImportModal: Math.random()
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
                let movieStore = this.props.movieStore;
                let stateStore = this.props.stateStore;
                stateStore!.showMovieModal = true;
                movieStore!.movies = response.data.result;
                stateStore!.code = code;
                stateStore!.regionName = regionName;
                this.setState({rerenderModal: Math.random()});
            })
            .catch(function (error: any) {
                console.log(error);
            });

    };

    changeDataStateCallback = (data: Record<string, MyMovie[]>) => {
        this.setState({
            myMovies: data
        })
    }

    show_import_modal = () => {
        this.props.stateStore!.setShowImportModal(true);
    }

    componentDidMount() {
        const importModal = document.getElementById("importModal");
        const movieModal = document.getElementById("myModal");
        window.onclick = (event) => {
            let stateStore = this.props.stateStore!;
            if (event.target === importModal) {
                stateStore.closeImportModal();
            }
            if (event.target === movieModal) {
                stateStore.toggleShowMovieModal();
            }
        };
        this.generateColors();
    }

    render() {
        return (
            <div className="map-container inner-map-container">
                <button onClick={this.show_import_modal}>Import</button>
                <Import changeDataStateCallback={this.changeDataStateCallback}
                        rerenderImport={this.state.rerenderImportModal}/>
                <div id="mappy">
                    <VectorMap
                        map={worldMill}
                        backgroundColor="#a5bfdd"
                        mapRef={this.myRef}
                        style={{
                            width: '100%',
                            height: '100%'
                        }}
                        series={{
                            regions:
                                [{
                                    attribute: 'fill',
                                    scale: {seen: '#c9dfaf', unseen: '#A8A8A8'},
                                }]
                        }}
                        regionStyle={{
                            hover: {fill: '#A8A8A8'},
                            initial: {fill: '#A8A8A8'}
                        }}
                        zoomOnScroll={false}
                        onRegionClick={this.onRegionClick}
                        className="map"
                    />

                    <MovieModal /** TODO rerender={this.state.rerenderModal}**//>
                </div>
            </div>
        );
    }
}

export default MyMoviesMap;