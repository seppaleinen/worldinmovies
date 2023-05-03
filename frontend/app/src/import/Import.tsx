import React from 'react';
import styles from './Import.module.scss';
import { useNavigate } from 'react-router-dom';

const Import = () => {
    const navigate = useNavigate();
    return (
        <div className={styles.container}>
            <div className={styles.mainText}>
                <h2>Import</h2>
                If you have some service where you log which movies you’ve seen
                and want to try and import them here, to see an updated map of which countries you’ve “visited”
            </div>
            <div className={styles.images}>
                <img className={styles.image} src="/trakt-wide-red-black.png" alt="Trakt"
                     onClick={() => navigate('/import/trakt')}/>
                <img className={styles.image} src="/IMDB-Logo.jpeg" alt="IMDB"
                     onClick={() => navigate("/import/imdb")}/>
            </div>
        </div>
    )
}

export default Import;