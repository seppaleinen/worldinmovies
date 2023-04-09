import React, {RefObject, useEffect, useState} from 'react';
import './MyMoviesMap.scss';
import {VectorMap} from "@react-jvectormap/core"
import {inject, observer} from "mobx-react";
import {IMapObject} from "@react-jvectormap/core/dist/types";
import customWorldMapJson from './customworldmap.json';
import {useNavigate} from "react-router-dom";
import MovieStore from "../stores/MovieStore";

const MyMoviesMap = inject('movieStore')
(observer(({movieStore}: { movieStore?: MovieStore}) => {
    let myRef: RefObject<IMapObject> = React.createRef();
    const [myMovies, setMyMovies] = useState<{}>({})
    const navigate = useNavigate();

    const onRegionClick = (event: any, code: string) => {
        // @ts-ignore
        let mapObject = myRef.current.getMapObject();
        mapObject.tip.hide(code);
        navigate("/country/" + code);
    };

    useEffect(() => {
        movieStore!.hydrateStore().then(() => {
            setMyMovies(movieStore!.myMovies);
            // @ts-ignore
            let mapObject = myRef.current!.getMapObject();
            const generateColors = () => {
                let colors: Record<string, string> = {}, key;

                if (myRef?.current) {
                    for (key in mapObject.regions) {
                        const found = myMovies !== undefined && key in myMovies;
                        colors[key] = (found ? 'seen' /* light green */ : 'unseen' /* gray */);
                    }
                }
                return colors;
            };
            mapObject.series.regions[0].setValues(generateColors());
        });
    }, [myMovies, movieStore, myRef])

    return (
        <div className="map-container inner-map-container">
            <div id="mappy">
                <VectorMap
                    map={customWorldMapJson}
                    backgroundColor="var(--dominant)"
                    mapRef={myRef}
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
                    onRegionClick={onRegionClick}
                    className="map"
                />
            </div>
        </div>
    );
}));

export default MyMoviesMap;