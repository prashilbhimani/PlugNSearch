import React, { Component } from 'react';
import { connect } from 'react-redux';
import { localGet } from "../actions";
import { Link } from 'react-router-dom';
import _ from 'lodash';
import Checkbox from './checkbox';

class PostsIndex extends Component {
  componentDidMount() {
    // automatically called by react lifecycle.
    // its a one time thing.
    this.props.localGet();
  }
  renderPosts() {
    return _.map(this.props.posts, post => {
      return (
        <li className="list-group-item" key={post.id}>
          { post.title }
        </li>
      );
    });// _ needed for mapping through object


  }
  render() {
    return (
      <div>
        <div className="text-xs-right">
          <Link className="btn btn-primary" to="/posts/new">
            Add a Post
          </Link>
        </div>
        <h3>Posts</h3>
        <ul className="list-group">
          {this.renderPosts()}
        </ul>
        {console.log(this.props.localget)}
        <Checkbox/>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return { posts: state.posts, localget: state.localget };
}

export default connect(mapStateToProps, {localGet: localGet})(PostsIndex); // you can also use mapDispatchToProps
