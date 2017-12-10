import setScroll from './set-scroll';

let _handleInsert = ( message, event, template ) => {
  Meteor.call( 'insertMessage', message, ( error ) => {
    if ( error ) {
      Bert.alert( error.reason, 'danger' );
    } else {
      event.target.value = '';
    }
  });
};

let _buildMessage = ( template, text ) => {
  return {
    destination: FlowRouter.getParam( 'channel' ).replace( '@', '' ),
    isDirect: template.isDirect.get(),
    message: text
  };
};

let _checkIfCanInsert = ( message, event ) => {
  return message !== '' && event.keyCode === 13;
};

let _getMessage = ( template ) => {
  let message = template.find( '[name="message"]' ).value;
  return message.trim();
};

export default function( event, template ) {
  let text      = _getMessage( template ),
      canInsert = _checkIfCanInsert( text, event );

  if ( canInsert ) {
    setScroll( 'messages' );
    _handleInsert( _buildMessage( template, text ), event, template );
  }
}
