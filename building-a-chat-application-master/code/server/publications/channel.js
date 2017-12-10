Meteor.publish( 'channel', function( isDirect, channel ) {
  check( isDirect, Boolean );
  check( channel, String );

  if ( isDirect ) {
    let user = Meteor.users.findOne( { username: channel.replace( '@', '' ) } );
    return Messages.find({
      $or: [ { owner: this.userId, to: user._id }, { owner: user._id, to: this.userId } ]
    });;
  } else {
    let selectedChannel = Channels.findOne( { name: channel } );
    return Messages.find( { channel: selectedChannel._id } );
  }
});
