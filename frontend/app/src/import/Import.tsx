import React from 'react';
import './Import.css';
import {inject, observer} from "mobx-react";
import FileUpload from './FileUpload';
import Trakt from './Trakt';
import {StoreType} from "../Types";

export interface ImportProps {
    rerenderImport: number;
    store?: StoreType;
    changeDataStateCallback: any;
}

export interface ImportState {
    rerenderImport: number;
}

@inject('store')
@observer
class Import extends React.Component<ImportProps, ImportState> {
    constructor(props: ImportProps) {
        super(props);
        this.state = {
            rerenderImport: Math.random()
        }
    }

    change_view_to_imdb = () => {
        this.props.store!.importView = 'IMDB';
    }

    change_view_to_trakt = () => {
        this.props.store!.importView = "TRAKT";
    }

    componentDidUpdate(prevProps: ImportProps) {
        if (this.props.rerenderImport !== prevProps.rerenderImport) {
            this.setState({rerenderImport: this.props.rerenderImport});
        }
        if (this.props.store!.showImportModal !== prevProps.store!.showImportModal) {
            this.setState({rerenderImport: this.props.rerenderImport});
        }
    }

    changePage = () => {
        switch (this.props.store!.importView) {
            case 'FIRST':
                return (
                    <div>
                        <div className="mainText">
                            <h2>Import</h2>
                            Choose how you want to import your data
                        </div>
                        <div className="images">
                            <img className="image" src="/static/trakt-wide-red-black.png" alt="Trakt"
                                 onClick={this.change_view_to_trakt}/>
                            <img className="image" src="/static/IMDB-Logo.jpeg" alt="IMDB"
                                 onClick={this.change_view_to_imdb}/>
                        </div>
                    </div>
                );
            case 'IMDB':
                return (
                    <FileUpload changeDataStateCallback={this.props.changeDataStateCallback} />
                );
            case 'TRAKT':
                return (
                    <Trakt/>
                );
            default:
                return (
                    <div>
                        Something went wrong here...
                    </div>
                );
        }
    }

    render() {
        const showModal = this.props.store!.showImportModal ? 'block' : 'none';
        return (
            <div id="importModal" style={{display: showModal}}>
                <div className="modal-content">
                    <span id="importModalClose" className='close'
                          onClick={this.props.store!.closeImportModal}>&times;</span>
                    {this.changePage()}
                </div>
            </div>
        )
    }
}

export default Import;
