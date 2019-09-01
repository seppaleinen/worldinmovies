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
          view: "FIRST",
          rerenderImport: Math.random()
        }
      }

      change_view_to_imdb = () => {
        this.setState({view: "IMDB"})
      }

      change_view_to_trakt = () => {
        this.setState({view: "TRAKT"})
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

      render() {
        const showModal = this.props.store.showImportModal ? 'block' : 'none';
        switch(this.state.view) {
          case 'FIRST':
            return (
              <div id="importModal"  style={{display: showModal}}>
                <div className="modal-content">
                  <span id="importModalClose" className='close' onClick={this.props.store.closeImportModal}>&times;</span>
                  <div>Import</div>
                  <div>
                    Choose how you want to import your data
                  </div>
                  <img src="https://cdn-images-1.medium.com/max/1500/1*Ve4N38AmTXhv7RrWba8LLw@2x.png" alt="asd"  onClick={this.change_view_to_trakt}/>
                  <img src="https://m.media-amazon.com/images/G/01/IMDb/BG_rectangle._CB1509060989_SY230_SX307_AL_.png" alt="sda" onClick={this.change_view_to_imdb}/>
                </div>
              </div>
            );
          case 'IMDB':
            return (
              <div id="importModal" className="modal-content" style={{display: showModal}}>
                <div className="modal-content">
                  <span className='close'>&times;</span>
                  <FileUpload changeDataStateCallback={this.props.changeDataStateCallback}/>
                </div>
              </div>
            );
          case 'TRAKT':
            return (
              <div id="importModal" className="modal-content" style={{display: showModal}}>
                <div className="modal-content">
                  <span className='close'>&times;</span>
                  <div>auth</div>
                </div>
              </div>
            );
        }
      }
    }
  )
)

export default Import;
