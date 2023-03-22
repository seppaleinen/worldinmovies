import React from "react";
import './Header.scss';
import {adminIcon, homeIcon, importMoviesIcon, searchIcon, worldMapIcon} from "./Svgs";

const menuButton = () => {
    return <div id={"menu"} className={"subnav"}>
        <label className="hamb" htmlFor="side-menu"><span className="hamb-line"></span></label>
        <label className={"button subnavbtn"} htmlFor="side-menu">
            <label htmlFor="side-menu">Menu</label>
        </label>
    </div>;
}

const adminButton = () => {
    return <a href={"/admin"} className={"button"}>
        {adminIcon()}
        Admin
    </a>
}

const importMoviesButton = (props: Props) => {
    return <div className={"button"} onClick={() => props.redirectToPage('import')}>
        {importMoviesIcon()}
        Import Movies
    </div>
}

const search = () => {
    return <div className={"search button"}>
        {searchIcon()}
        <span className="input" role="textbox" contentEditable>
        </span>
    </div>;
}

const worldMapButton = (props: Props) => {
    return <button className={"button worldmap"} onClick={() => props.redirectToPage('worldmap')}>
        {worldMapIcon()}
        World Map
    </button>;
}

const homeButton = (props: Props) => {
    return <button className={"button home"} onClick={() => props.redirectToPage('welcome')}>
        {homeIcon()}
        The World in Movies
    </button>;
}


const Header = (props: Props) => {
    return (
        <header>
            <div className={"left-headers"}>
                <input className="side-menu" type="checkbox" id="side-menu"/>
                {menuButton()}
                {homeButton(props)}
                <nav>
                    {worldMapButton(props)}
                    <div className={"subnav-content"}>
                        {importMoviesButton(props)}
                        {adminButton()}
                    </div>
                </nav>
            </div>
            <div className={"right-headers"}>
                {search()}
            </div>
        </header>
    );
}

export interface Props {
    redirectToPage: (page: string) => void;
}

export default Header;