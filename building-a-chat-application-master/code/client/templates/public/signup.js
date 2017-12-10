import signup from '../../modules/signup';
import sanitizeUsername from '../../modules/sanitize-username';

Template.signup.onRendered( () => {
  signup({ form: '#signup', template: Template.instance() });
});

Template.signup.events({
  'submit form': ( event ) => event.preventDefault(),
  'keyup [name="username"]' ( event ) {
    let value     = event.target.value,
        formatted = sanitizeUsername( value );
    event.target.value = formatted;
  }
});
