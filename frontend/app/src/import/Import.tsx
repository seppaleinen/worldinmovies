import React from 'react';
import './Import.scss';
import { useNavigate } from 'react-router-dom';

export interface ImportProps {
}

const Import = () => {
    const navigate = useNavigate();
    return (
        <div>
            <div className="mainText">
                <h2>Import</h2>
                Choose how you want to import your data
            </div>
            <div className="images">
                <img className="image" src="/trakt-wide-red-black.png" alt="Trakt"
                     onClick={() => navigate('/import/trakt')}/>
                <img className="image" src="/IMDB-Logo.jpeg" alt="IMDB"
                     onClick={() => navigate("/import/imdb")}/>
            </div>
        </div>
    )
}

export default Import;