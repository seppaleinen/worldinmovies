import React from "react";
import './Header.scss';
import {homeIcon, importMoviesIcon, menuIcon, searchIcon, worldMapIcon} from "./Svgs";

const menuButton = (props: Props) => {
    return <div id={"menu"}>
        <button className={"button"}>
            {menuIcon()}
            Menu
        </button>
        <div className={"menu-content"}>
            {importMoviesButton(props)}
            {adminButton()}
        </div>
    </div>;
}

const adminButton = () => {
    return <div className={"button"} >
        <a href={"/admin"}>Admin</a>
    </div>
}

const importMoviesButton = (props: Props) => {
    return <div className={"button"} onClick={() => props.redirectToPage('import')}>
        {importMoviesIcon()}
        Import Movies
    </div>
}

const search = () => {
    return <div className={"searchbutton"}>
        {searchIcon()}
        <input type="text" placeholder="Search"></input>
    </div>;
}

const worldMapButton = (props: Props) => {
    return <button className={"button"} onClick={() => props.redirectToPage('worldmap')}>
        {worldMapIcon()}
        World Map
    </button>;
}

const homeButton = (props: Props) => {
    return <button className={"button"} onClick={() => props.redirectToPage('welcome')}>
        {homeIcon()}
        The World in Movies
    </button>;
}


const Header = (props: Props) => {
    return (
        <nav>
            <div id="nav-links">
                <div id="nav-links-left">
                    {homeButton(props)}
                    {worldMapButton(props)}
                </div>
                <div id="nav-links-right">
                    {search()}
                    {menuButton(props)}
                </div>
            </div>
        </nav>
    );
}

export interface Props {
    redirectToPage: (page: string) => void;
}

export default Header;