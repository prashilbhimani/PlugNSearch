import axios from 'axios';
export const FETCH_POSTS = 'fetch_posts';
export const CREATE_POSTS = 'create_posts';
export const LOCAL_GET = 'local_get';

const ROOT_URL = 'http://reduxblog.herokuapp.com/api';
const API_KEY = '?key=paperbash1432;'

export function fetchPosts() {
  const request = axios.get(
    `${ROOT_URL}/posts${API_KEY}`,
    {
      timeout: 10000,
      withCredentials: true,
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }
  );
  // console.log(`req: ${request}`)
  return {
    type: FETCH_POSTS,
    payload: request
  };
}

// Create the XHR object.
function createCORSRequest(method, url) {
  var xhr = new XMLHttpRequest();
  if ("withCredentials" in xhr) {
    // XHR for Chrome/Firefox/Opera/Safari.
    xhr.open(method, url, true);
  } else if (typeof XDomainRequest != "undefined") {
    // XDomainRequest for IE.
    xhr = new XDomainRequest();
    xhr.open(method, url);
  } else {
    // CORS not supported.
    xhr = null;
  }
  return xhr;
}


// Make the actual CORS request.
function makeCorsRequest() {
  // This is a sample server that supports CORS.
  var url = 'http://localhost:9000';

  var xhr = createCORSRequest('GET', url);
  if (!xhr) {
    alert('CORS not supported');
    return;
  }

  // Response handlers.
  xhr.onload = function() {
    var text = xhr.responseText;
    console.log(`Load Text is: ${text}`);
  };

  xhr.onerror = function() {
    alert('Woops, there was an error making the request.');
  };

  xhr.send();
}

export function localGet() {
  makeCorsRequest();
  return {
    type: LOCAL_GET,
    payload: "hello"
  };
};



export function createPost(values) {
  const request = axios.post(`${ROOT_URL}/posts${API_KEY}`, values);

  return {
    type: CREATE_POST,
    payload: request
  }
}
