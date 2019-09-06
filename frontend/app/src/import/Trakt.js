import React from 'react';
import './Import.css';
import { inject, observer } from "mobx-react";
import Trakt from 'trakt.tv';
import { OAuth2PopupFlow } from 'oauth2-popup-flow';

var TraktComponent = inject("store")(
  observer(
    class TraktComponent extends React.Component {
      constructor(props) {
        super(props);
        this.options = {
          client_id: '5c4055d25c6411203466af53318d3cf1f5199d15dbe56037a73801dca84494e7',
          client_secret: '0084c1b3f631a4cf0306c150c9cae3599a5b628d3cd7e4239d43d044fdda236c',
          redirect_uri: 'http://seppa.duckdns.org:8888',   // defaults to 'urn:ietf:wg:oauth:2.0:oob'
          api_url: 'https://api.trakt.tv'        // defaults to 'https://api.trakt.tv'
        };

        this.trakt = new Trakt(this.options);
        interface TokenPayload {
          exp: number;
          other: string;
          stuff: string;
          username: string;
        };
/**
        const auth = new OAuth2PopupFlow<TokenPayload>({
          authorizationUri: this.trakt.get_url(),
          clientId: this.options.client_id,
          redirectUri: this.options.redirect_uri,
          scope: 'openid profile',
        })
        **/
      }

      onCode = (code) => {
        console.log("Thank you" + code);
      }

      render() {

        return (
          <div>asd
          </div>
        )
      }
    }
  )
)

export default TraktComponent;