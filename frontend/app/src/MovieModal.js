import React from 'react';
import { inject, observer } from "mobx-react";

var MovieModal = inject("store")(
  observer(
    class MovieModal extends React.Component {
      constructor(props) {
        super(props);
        this.state = {rerender: Math.random()};
      }

      renderTopMovies(data) {
        return (
          data.map(item =>
              <tr key={item['imdb_id']}>
                <td></td>
                <td><a href={"https://www.imdb.com/title/" + item['imdb_id']}>{item['original_title']}</a></td>
                <td>{item['vote_average']}</td>
              </tr>
          )
        )
      }

      componentDidUpdate(prevProps) {
        if(this.props.rerender !== prevProps.rerender) {
          this.setState({rerender: this.props.rerender});
        }
      }

      is_movie_from_country(movie, code) {
        return movie.country_codes.find(country_code => {return code === country_code;});
      }

      shouldIRenderMyMovies(){
        return this.props.store.myMovies !== undefined && this.props.store.myMovies.length !== 0;
      }

      renderMyMovies(data) {
        let rows = data
          .filter(movie => this.is_movie_from_country(movie, this.props.store.code))
          .sort((a, b) => (a.personal_rating > b.personal_rating) ? -1 : 1)
          .slice(0, 10)
          .map(item => (
            <tr key={item['imdb_id']}>
              <td></td>
              <td><a href={"https://www.imdb.com/title/" + item['imdb_id']}>{item['title']}</a></td>
              <td>{item['personal_rating']}</td>
            </tr>
          ));

        return (
          <div id="myMoviesTable"><h2>Your top ranked movies from {this.props.store.regionName}</h2>
            <table className="modal-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Title</th>
                  <th>Your rating</th>
                </tr>
              </thead>
              <tbody>
                {rows}
              </tbody>
            </table>
          </div>
        )
      }

      componentDidMount() {
        var modal = document.getElementById("myModal");

        // Get the <span> element that closes the modal
        var span = document.getElementsByClassName("close")[0];

        // When the user clicks on <span> (x), close the modal
        span.onclick = function() {
          this.props.store.showMovieModal = false;
          this.setState({rerender: Math.random()});
          this.props.callback();
        }.bind(this);;

        // When the user clicks anywhere outside of the modal, close it
        window.onclick = function(event) {
          if (event.target === modal) {
            this.props.store.showMovieModal = false;
            this.setState({rerender: Math.random()});
          }
        }.bind(this);
      }

      render() {
        const showModal = this.props.store.showMovieModal ? 'block' : 'none';
        return (
          <div id="myModal" className="modal" style={{display: showModal}}>
            <div className="modal-content">
              <span className='close'>&times;</span>
              <div id="modal-text">
                <section id="containingSection">
                  <div id="rankedMoviesTable"><h2>Top ranked movies from {this.props.store.regionName}</h2>
                    <table className="modal-table">
                      <thead>
                        <tr>
                          <th>#</th>
                          <th>Title</th>
                          <th>Rating</th>
                        </tr>
                      </thead>
                      <tbody>
                        {this.renderTopMovies(this.props.store.movies)}
                      </tbody>
                    </table>
                  </div>
                  {this.shouldIRenderMyMovies() && this.renderMyMovies(this.props.store.myMovies)}
                </section>
              </div>
            </div>
          </div>
        )
      }
    }
  )
)

export default MovieModal;