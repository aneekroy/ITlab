export default function( value ) {
  return value.replace( /[^A-Za-z0-9\s]/g, '' ).toLowerCase().trim();
}
