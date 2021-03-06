import React from 'react';
import ReactDOM from 'react-dom';
import Import from './Import';
import { Provider } from "mobx-react";
import Store from "../MobxStore";

const stores  = {
  store: new Store()
}

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(  <Provider {...stores}><Import /></Provider>, div);
  ReactDOM.unmountComponentAtNode(div);
});
