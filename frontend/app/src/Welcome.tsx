import React from 'react';
import styles from './Welcome.module.scss';
import {worldMapLogo} from "./Svgs";

const Welcome = () => {
    return (
        <div>
            <div className={styles.mainContainer}>
                {worldMapLogo(styles.worldmaplogosvg)}
                <div>
                    <h1>Welcome to the World in Movies</h1>
                    <div className={"text-content"}>
                        <h4>
                            If you're searching for recommendations on foreign movies,
                        </h4>
                        <h4>
                            or just curious about how well travelled you are in the movie world.
                        </h4>
                        <h4>
                            This is the page for you.
                        </h4>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Welcome;