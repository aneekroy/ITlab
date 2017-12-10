"use strict";

// for better performance - to avoid searching in DOM
const inputElement = document.getElementById('input');
const contentElement = document.getElementById('content');
const statusElement = document.getElementById('status');

// my color assigned by the server
var myColor = false;
// my name sent to the server
var myName = false;

// if user is running mozilla then use it's built-in WebSocket
window.WebSocket = window.WebSocket || window.MozWebSocket;

// if browser doesn't support WebSocket, just show
// some notification and exit
if (!window.WebSocket) {
  contentElement.innerHTML = "<p>Sorry, your browser doesn't support websocket.</p>";
  inputElement.style = "display: none";
  statusElement.style = "display: none";
}
// open connection
const connection = new WebSocket('ws://127.0.0.1:1337');

connection.addEventListener('open', function(e) {
  // first we want users to enter their names
  inputElement.removeAttribute('disabled');
  statusElement.innerHTML = 'Choose name:';
});

connection.addEventListener('error', function (error) {
  // just in there were some problems with connection...
  contentElement.innerHTML = '<p>Sorry, but there\'s some problem with your connection, or the server is down.</p>';
});

// most important part - incoming messages
connection.addEventListener('message', function (message) {
  // try to parse JSON message. Because we know that the server
  // always returns JSON this should work without any problem but
  // we should make sure that the massage is not chunked or
  // otherwise damaged.
  var json;
  try {
    json = JSON.parse(message.data);
  } catch (e) {
    console.log('Invalid JSON: ', message.data);
    return;
  }

  // NOTE: if you're not sure about the JSON structure
  // check the server source code above
  // first response from the server with user's color
  if (json.type === 'color') {
    myColor = json.data;
    statusElement.innerHTML = myName + ': ';
    statusElement.style.color = myColor;
    inputElement.removeAttribute('disabled');
    inputElement.focus();
    // from now user can start sending messages
  } else if (json.type === 'history') {
    // insert every single message to the chat window
    for (var i=0; i < json.data.length; i++) {
      addMessage(json.data[i].author, json.data[i].text, json.data[i].color, new Date(json.data[i].time));
    }
  } else if (json.type === 'message') {
    // let the user write another message
    inputElement.removeAttribute('disabled');
    addMessage(json.data.author, json.data.text, json.data.color, new Date(json.data.time));
  } else {
    console.log('Hmm..., I\'ve never seen JSON like this:', json);
  }
});

/**
 * Send message when user presses Enter key
 */
input.addEventListener('keydown', function(e) {
  if (e.keyCode === 13) {
    const msg = inputElement.value;

    if (!msg) {
      return;
    }

    // send the message as an ordinary text
    connection.send(msg);
    inputElement.value = '';

    // disable the input field to make the user wait until server
    // sends back response
    inputElement.setAttribute('disabled', 'disabled');

    // we know that the first message sent from a user their name
    if (!myName) {
      myName = msg;
    }
  }
});

/**
 * This method is optional. If the server wasn't able to
 * respond to the in 3 seconds then show some error message
 * to notify the user that something is wrong.
 */
setInterval(function() {
  if (connection.readyState !== 1) {
    statusElement.innerHTML = 'ERROR';
    inputElement.setAttribute('disabled', 'disabled');
    inputElement.value = 'Unable to communicate with the WebSocket server.';
  }
}, 3000);

/**
 * Add message to the chat window
 */
function addMessage(author, message, color, dt) {
  contentElement.innerHTML += '<p><span style="color:' + color + '">'
      + author + '</span> @ ' + (dt.getHours() < 10 ? '0'
      + dt.getHours() : dt.getHours()) + ':'
      + (dt.getMinutes() < 10
        ? '0' + dt.getMinutes() : dt.getMinutes())
      + ': ' + message + '</p>';
  contentElement.scrollTop = contentElement.clientHeight;
}
