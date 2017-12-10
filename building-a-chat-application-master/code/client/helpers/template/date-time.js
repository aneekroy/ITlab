Template.registerHelper( 'formatDateTime', ( timestamp, format ) => {
  if ( timestamp && format ) {
    return moment( timestamp ).format( format );
  }
});

Template.registerHelper( 'formatDateTimeLocal', ( timestamp, timezone, format ) => {
  if ( timestamp && timezone && format ) {
    return moment( timestamp ).tz( timezone ).format( format );
  }
});

Template.registerHelper( 'messageTimestamp', ( timestamp ) => {
  if ( timestamp ) {
    let today         = moment().format( 'YYYY-MM-DD' ),
        datestamp     = moment( timestamp ).format( 'YYYY-MM-DD' ),
        isBeforeToday = moment( today ).isAfter( datestamp ),
        format        = isBeforeToday ? 'MMMM Do, YYYY hh:mm a' : 'hh:mm a';
    return moment( timestamp ).format( format );
  }
});
