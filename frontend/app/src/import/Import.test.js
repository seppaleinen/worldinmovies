import React from 'react';
import ReactDOM from 'react-dom';
import Import from './Import';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Import />, div);
  ReactDOM.unmountComponentAtNode(div);
});
