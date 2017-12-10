Template.sidebar.onCreated( () => {
  let template = Template.instance();
  template.subscribe( 'sidebar' );
});

Template.sidebar.helpers({
  currentChannel( name ) {
    let current = FlowRouter.getParam( 'channel' );
    if ( current ) {
      return current === name || current === `@${ name }` ? 'active' : false;
    }
  },
  channels() {
    let channels = Channels.find();
    if ( channels ) {
      return channels;
    }
  },
  users() {
    let users = Meteor.users.find( { _id: { $ne: Meteor.userId() } } );
    if ( users ) {
      return users;
    }
  },
  fullName( name ) {
    if ( name ) {
      return `${ name.first } ${ name.last }`;
    }
  }
});
