import React from "react";
import styles from './Header.module.scss';
import {adminIcon, homeIcon, importMoviesIcon, worldMapIcon} from "./Svgs";

const menuButton = () => {
    return <div id={"menu"} className={styles.subnav}>
        <label className={styles.hamb} htmlFor="side-menu"><span className={styles.hambLine}></span></label>
        <label className={styles.button} htmlFor="side-menu">
            <label htmlFor="side-menu">Menu</label>
        </label>
    </div>;
}

const adminButton = () => {
    return <a href={"/admin"} className={styles.button}>
        {adminIcon()}
        Admin
    </a>
}

const importMoviesButton = (props: Props) => {
    return <div className={styles.button} onClick={() => props.redirectToPage('import')}>
        {importMoviesIcon()}
        Import Movies
    </div>
}

/**
const search = () => {
    return <div className={`${styles.search} ${styles.button}`}>
        {searchIcon()}
        <span className={styles.input} role="textbox" contentEditable>
        </span>
    </div>;
}
 **/

const worldMapButton = (props: Props) => {
    return <button className={`${styles.worldmap} ${styles.button}`} onClick={() => props.redirectToPage('worldmap')}>
        {worldMapIcon()}
        World Map
    </button>;
}

const homeButton = (props: Props) => {
    return <button className={`${styles.button} ${styles.hoem}`} onClick={() => props.redirectToPage('welcome')}>
        {homeIcon()}
        The World in Movies
    </button>;
}


const Header = (props: Props) => {
    return (
        <header>
            <div className={styles.leftheaders}>
                <input className={styles.sideMenu} type="checkbox" id="side-menu"/>
                {menuButton()}
                {homeButton(props)}
                <nav className={styles.nav}>
                    {worldMapButton(props)}
                    <div className={styles.subnavContent}>
                        {importMoviesButton(props)}
                        {adminButton()}
                    </div>
                </nav>
            </div>
            <div className={styles.rightHeaders}>
                {/*search()*/}
            </div>
        </header>
    );
}

export interface Props {
    redirectToPage: (page: string) => void;
}

export default Header;