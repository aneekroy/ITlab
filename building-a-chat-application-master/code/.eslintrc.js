module.exports = {
  'env': {
    'es6': true,
    'meteor': true,
    'node': true
  },
  'extends': 'eslint:recommended',
  'ecmaFeatures': {
    'jsx': true,
    'modules': true,
    'experimentalObjectRestSpread': true
  },
  'plugins': [],
  'globals': {
    'Bert': true,
    'BlazeLayout': true,
    'Channels': true,
    'Counts': true,
    'document': true,
    'FlowRouter': true,
    'Messages': true,
    'moment': true,
    'parseMarkdown': true,
    'ReadLog': true,
    'Roles': true,
    'SimpleSchema': true
  },
  'rules': {
    'comma-dangle': [ 2, 'never' ],
    'computed-property-spacing': [ 2, 'always' ],
    'eqeqeq': [ 2, 'smart' ],
    'indent': [ 2, 2, { 'VariableDeclarator': 2 } ],
    'linebreak-style': [ 2, 'unix' ],
    'no-console': [ 0 ],
    'no-unneeded-ternary': [ 2 ],
    'object-curly-spacing': [ 2, 'always' ],
    'quotes': [ 2, 'single' ],
    'semi': [ 2, 'always' ],
    'space-infix-ops': [ 2 ]
  }
};
