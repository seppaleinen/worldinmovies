import React from 'react';
import './Import.css';
import { inject, observer } from "mobx-react";
import FileUpload from './FileUpload';

var Import = inject("store")(
  observer(
    class Import extends React.Component {
      constructor(props) {
        super(props);
        this.state = {
          rerenderImport: Math.random()
        }
      }

      change_view_to_imdb = () => {
        this.props.store.importView = 'IMDB';
      }

      change_view_to_trakt = () => {
        this.props.store.importView = "TRAKT";
      }

      componentDidUpdate(prevProps) {
        if(this.props.rerenderImport !== prevProps.rerenderImport) {
          this.setState({rerenderImport: this.props.rerenderImport});
        }
        if(this.props.store.showImportModal !== prevProps.store.showImportModal) {
          this.setState({rerenderImport: this.props.rerenderImport});
        }
      }

      componentDidMount() {
        // When the user clicks on <span> (x), close the modal
        document.getElementById("importModalClose").onclick = () => {
          this.props.store.closeImportModal();
        };
      }

      changePage = () => {
        switch(this.props.store.importView) {
          case 'FIRST':
            return (
              <div>
                <div>Import</div>
                <div>
                  Choose how you want to import your data
                </div>
                <img src="https://cdn-images-1.medium.com/max/1500/1*Ve4N38AmTXhv7RrWba8LLw@2x.png" alt="asd"  onClick={this.change_view_to_trakt}/>
                <img src="https://m.media-amazon.com/images/G/01/IMDb/BG_rectangle._CB1509060989_SY230_SX307_AL_.png" alt="sda" onClick={this.change_view_to_imdb}/>
              </div>
            );
          case 'IMDB':
            return (
              <div>
                <FileUpload changeDataStateCallback={this.props.changeDataStateCallback}/>
              </div>
            );
          case 'TRAKT':
            return (
              <div>
                <div>auth</div>
              </div>
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
        const showModal = this.props.store.showImportModal ? 'block' : 'none';
        return (
          <div id="importModal"  style={{display: showModal}}>
            <div className="modal-content">
              <span id="importModalClose" className='close' onClick={this.props.store.closeImportModal}>&times;</span>
              {this.changePage()}
            </div>
          </div>
        )
      }
    }
  )
)

export default Import;
