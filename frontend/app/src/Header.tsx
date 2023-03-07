import React from "react";
import './Header.scss';
import {adminIcon, homeIcon, importMoviesIcon, menuIcon, searchIcon, worldMapIcon} from "./Svgs";

const menuButton = (props: Props) => {
    return <div id={"menu"} className={"subnav"}>
        <input className="subnav-menu" type="checkbox" id="subnav-menu"/>
        <label className={"button subnavbtn"} htmlFor="subnav-menu">
            {menuIcon()}
            Menu
        </label>
        <div className={"subnav-content"}>
            {importMoviesButton(props)}
            {adminButton()}
        </div>
    </div>;
}

const adminButton = () => {
    return <div className={"button"}>
        <a href={"/admin"}>
            {adminIcon()}
            Admin
        </a>
    </div>
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
            {homeButton(props)}

            {search()}
            <input className="side-menu" type="checkbox" id="side-menu"/>
            <label className="hamb" htmlFor="side-menu"><span className="hamb-line"></span></label>
            <nav>
                {worldMapButton(props)}
                {menuButton(props)}
            </nav>
        </header>
    );
}

export interface Props {
    redirectToPage: (page: string) => void;
}

export default Header;