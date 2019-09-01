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
        if(this.props.store.showMovieModal !== prevProps.store.showMovieModal) {
          this.setState({rerender: this.props.rerender});
        }
      }

      shouldIRenderMyMovies(){
        return this.props.store.myMovies !== undefined && this.props.store.myMovies.length !== 0 && this.props.store.code in this.props.store.myMovies;
      }

      renderMyMovies(data) {
        let rows = data[this.props.store.code].slice()
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
        // When the user clicks on <span> (x), close the modal
        document.getElementById("closeMovieModalButton").onclick = () => {
          this.props.store.toggleShowMovieModal();
        }
      }

      render() {
        const showModal = this.props.store.showMovieModal ? 'block' : 'none';
        return (
          <div id="myModal" className="modal" style={{display: showModal}}>
            <div className="modal-content">
              <span id="closeMovieModalButton" className='close movieModalClose'>&times;</span>
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