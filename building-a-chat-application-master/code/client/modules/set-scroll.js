export default function( containerId ) {
  let messages = document.getElementById( containerId );
  setTimeout( () => { messages.scrollTop = messages.scrollHeight; }, 300 );
}
