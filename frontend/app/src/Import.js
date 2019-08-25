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
          view: "FIRST"
        }
      }

      change_view_to_imdb = () => {
        this.setState({view: "IMDB"})
      }

      change_view_to_trakt = () => {
        this.setState({view: "TRAKT"})
      }

      render() {
        switch(this.state.view) {
          case 'FIRST':
            return (
              <div className="import">
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
              <div className="import">
                <FileUpload changeDataStateCallback={this.props.changeDataStateCallback}/>
              </div>
            );
          case 'TRAKT':
            return (
              <div className="import">
                <div>auth</div>
              </div>
            );
        }
      }
    }
  )
)

export default Import;
