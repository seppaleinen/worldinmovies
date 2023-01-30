import React, {MutableRefObject} from 'react';
import './MyMoviesMap.css';
import MovieModal from './MovieModal';
import Import from './import/Import';
import {VectorMap} from "@react-jvectormap/core"
import axios, {AxiosResponse} from 'axios';
import {inject, observer} from "mobx-react";
import {worldMill} from "@react-jvectormap/world";
import {MyMovieMapState, Props} from "./Types";
import {IMapObject} from "@react-jvectormap/core/dist/types";

@inject('store')
@observer
class MyMoviesMap extends React.Component<Props, MyMovieMapState> {
    myRef = React.createRef<MutableRefObject<IMapObject> & null>();
    state: MyMovieMapState = {
        data: {found: {}, not_found: []},
        rerenderModal: 0,
        rerenderImportModal: 0
    };

    constructor(props: Props) {
        super(props);
        this.state = {
            data: props.data,
            rerenderModal: Math.random(),
            rerenderImportModal: Math.random()
        }
    }

    generateColors = () => {
        let colors: Record<string, string> = {}, key;

        // @ts-ignore
        for (key in this.myRef.current.getMapObject().regions) {
            // @ts-ignore
            const found = key in this.state.data.found;
            const color = (found ? 'seen' /* light green */ : 'unseen' /* gray */);
            colors[key] = color;
        }
        return colors;
    };

    componentDidUpdate(prevProps: Props, prevState: MyMovieMapState) {
        if (this.state.data !== prevState.data) {
            // @ts-ignore
            this.myRef.current.getMapObject().series.regions[0].setValues(this.generateColors());
        }
    }

    onRegionClick = (event: any, code: string) => {
        // @ts-ignore
        const regionName = this.myRef.current.getMapObject().getRegionName(code);
        axios.get("/backend/view/best/" + code.toUpperCase(), {timeout: 5000})
            .then((response: AxiosResponse) => {
                this.props.store!.showMovieModal = true;
                this.props.store!.movies = response.data.result;
                this.props.store!.code = code;
                this.props.store!.regionName = regionName;
                this.setState({rerenderModal: Math.random()});
            })
            .catch(function (error: any) {
                console.log(error);
            });

    };

    changeDataStateCallback = (data: any) => {
        this.setState({
            data: data
        })
    }

    show_import_modal = () => {
        this.props.store!.showImportModal = true;
    }

    componentDidMount() {
        const importModal = document.getElementById("importModal");
        const movieModal = document.getElementById("myModal");
        window.onclick = (event) => {
            if (event.target === importModal) {
                this.props.store!.closeImportModal();
            }
            if (event.target === movieModal) {
                this.props.store!.toggleShowMovieModal();
            }
        };

    }

    render() {
        return (
            <div className="map-container inner-map-container">
                <button onClick={this.show_import_modal}>Import</button>
                <Import changeDataStateCallback={this.changeDataStateCallback}
                        rerenderImport={this.state.rerenderImportModal}
                        store={this.props.store}/>
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