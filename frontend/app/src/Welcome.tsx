import React from 'react';
import styles from './Welcome.module.scss';
import {worldMapLogo} from "./Svgs";

const Welcome = () => {
    return (
        <div>
            <div className={styles.mainContainer}>
                {worldMapLogo(styles.worldmaplogosvg)}
                <div>
                    <h1>Discover the World through Film</h1>
                    <div>
                        <h4>
                            At Worldinmovies, we're passionate about helping movie lovers discover foreign films from
                            around the world.
                        </h4>

                        <h4>
                            With our user-friendly platform, you can easily explore new cultures and expand your
                            horizons through the magic of cinema.
                        </h4>
                        <h4>
                            Join us on a journey of discovery and let the power of film transport you to new places and
                            perspectives.
                        </h4>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Welcome;