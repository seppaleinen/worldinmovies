import React from 'react';
import './Import.css';
import { inject, observer } from "mobx-react";

var Trakt = inject("store")(
  observer(
    class Trakt extends React.Component {
      render() {
        return (
          <div>
            <div>auth</div>
          </div>
        )
      }
    }
  )
)

export default Trakt;