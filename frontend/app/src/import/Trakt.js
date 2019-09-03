import React from 'react';
import './Import.css';
import { inject, observer } from "mobx-react";

var Trakt = inject("store")(
  observer(
    class Trakt extends React.Component {
      render() {
        return (
          <div>
            <div>There's nothing here yet. But hopefully soon!</div>
          </div>
        )
      }
    }
  )
)

export default Trakt;