import React from 'react';
import './Import.scss';

export interface ImportProps {
    redirectToPage: (data: string) => void;
}

const Import = (props: ImportProps) => {
    return (
        <div>
            <div className="mainText">
                <h2>Import</h2>
                Choose how you want to import your data
            </div>
            <div className="images">
                <img className="image" src="/static/trakt-wide-red-black.png" alt="Trakt"
                     onClick={() => props.redirectToPage('trakt')}/>
                <img className="image" src="/static/IMDB-Logo.jpeg" alt="IMDB"
                     onClick={() => props.redirectToPage('imdb')}/>
            </div>
        </div>
    )
}

export default Import;