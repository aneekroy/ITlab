import setScroll from './set-scroll';

let _establishSubscription = ( template, isDirect, channel ) => {
  template.subscribe( 'channel', isDirect, channel, () => {
    setScroll( 'messages' );
    setTimeout( () => { template.loading.set( false ); }, 300 );
  });
};

let _handleSwitch = ( template ) => {
  let channel = FlowRouter.getParam( 'channel' );

  if ( channel ) {
    let isDirect = channel.includes( '@' );
    template.isDirect.set( isDirect );
    template.loading.set( true );
    _establishSubscription( template, isDirect, channel );
  }
};

let _setupReactiveVariables = ( template ) => {
  template.isDirect = new ReactiveVar();
  template.loading  = new ReactiveVar( true );
};

export default function( template ) {
  _setupReactiveVariables( template );
  Tracker.autorun( () => { _handleSwitch( template ); } );
}
