<div class="note">
  <h3>Experimental Usage of Meteor 1.3 <i class="fa fa-warning"></i></h3>
  <p>The code for this recipe is based on Meteor 1.3 and <a href="https://github.com/themeteorchef/base/tree/4.0.0">Base v4.0.0</a> (an upcoming release of The Meteor Chef's starter kit) which is currently in beta as of writing. While the code here is stable and works locally, it may be unstable in certain environments. When Meteor 1.3 is officially released, this repository will be updated to the stable version.</p>
</div>

<div class="note">
  <h3>Additional Packages <i class="fa fa-warning"></i></h3>
  <p>This recipe relies on several other packages that come as part of <a href="http://themeteorchef.com/base">Base</a>, the boilerplate kit used here on The Meteor Chef. The packages listed above are merely recipe-specific additions to the packages that are included by default in the kit. Make sure to reference the <a href="https://github.com/themeteorchef/building-a-chat-application/blob/master/code/.meteor/packages">Packages Included list</a> for Base to ensure you have fulfilled all of the dependencies.</p>
</div>

<div class="note info">
  <h3>Pre-Written Code <i class="fa fa-info"></i></h3>
  <p><strong>Heads up</strong>: this recipe relies on some code that has been pre-written for you, <a href="https://github.com/themeteorchef/building-a-chat-application">available in the recipe's repository on GitHub</a>. During this recipe, our focus will only be on implementing a simple chat application. If you find yourself asking "we didn't cover that, did we?", make sure to check the source on GitHub.</p>
</div>

### Prep
- **Time**: ~3 hours
- **Difficulty**: Intermediate
- **Additional knowledge required**: [ES2015](https://themeteorchef.com/blog/what-is-es2015/) [basics](https://themeteorchef.com/snippets/common-meteor-patterns-in-es2015/), [Using Collection2](https://themeteorchef.com/snippets/using-the-collection2-package/)

### What are we building?
MegaCorp is a Fortune 100 company on the brink of world domination. Nobody is entirely certain what they do and are afraid to question their motives. Spooky. 
MegaCorp's Interactive Media Task Force Division Committee have gotten in touch because they need an easy way for employees to more easily communicate. They tried to use [Slack](https://slack.com/), but their CTO said it was "far too much fun for a serious work environment."

Right now, MegaCorp's needs are limited. They're looking for two big features: a channel for the entire company and direct messages. Beyond this, MegaCorp has also alluded to Markdown being okay as long as it behaves similar to Slack (no `h1-h6` tags or inline images with `![]()`). They also want rigid control over new channel creation, so they've expressed a desire to keep that locked down.

Even though our feature set is limited, we've got a lot of work to do! Let's get to it.

### Ingredients
There are no additional packages required for this recipe! Everything we'll be building makes use of the [existing packages installed in Base](https://github.com/themeteorchef/building-a-chat-application/blob/master/code/.meteor/packages), or Meteor core features.

### Defining our collections
To get started, let's focus on wiring up our collections. Because MegaCorp's needs are so stringent, our data models are pretty simple. We're only going to need two collections to get this working: `Channels` and `Messages`. Let's take a look at `Channels` first as it's pretty simple.

<p class="block-header">/collections/channels.js</p>

```javascript
Channels = new Mongo.Collection( 'channels' );

Channels.allow({
  insert: () => false,
  update: () => false,
  remove: () => false
});

Channels.deny({
  insert: () => true,
  update: () => true,
  remove: () => true
});

let ChannelsSchema = new SimpleSchema({
  'name': {
    type: String,
    label: 'The name of the channel.'
  }
});

Channels.attachSchema( ChannelsSchema );
```

Not much to it! So what's happening here? Up top we're defining our collection and then we make sure to lock down our allow and deny rules for security sake. Why? Allow and deny rules are known to be [error-prone](https://www.discovermeteor.com/blog/allow-deny-challenge-results/) and have the potential to leave your application open to unwanted activity. Because of this, here, we lock these down on the client so all of our database operations are forced to take place on the server (i.e., we'll rely on [Meteor methods](http://docs.meteor.com/#/full/meteor_methods) to handle database operations from the client).

Down below this, we [get our schema set up](https://themeteorchef.com/snippets/using-the-collection2-package/). For our needs, channels are fairly barebones. All we'll need here is a name for our channel. Because the channel's `_id` field is automatically handled for us, we don't need to specify it here. Once we have this set up, we're all set after we attach our schema. That one was pretty easy. Our `Messages` collection is a little more complex, let's take a look.

<p class="block-header">/collections/messages.js</p>

```javascript
Messages = new Mongo.Collection( 'messages' );

Messages.allow({
  insert: () => false,
  update: () => false,
  remove: () => false
});

Messages.deny({
  insert: () => true,
  update: () => true,
  remove: () => true
});

let MessagesSchema = new SimpleSchema({
  'channel': {
    type: String,
    label: 'The ID of the channel this message belongs to.',
    optional: true
  },
  'to': {
    type: String,
    label: 'The ID of the user this message was sent directly to.',
    optional: true
  },
  'owner': {
    type: String,
    label: 'The ID of the user that created this message.'
  },
  'timestamp': {
    type: Date,
    label: 'The date and time this message was created.'
  },
  'message': {
    type: String,
    label: 'The content of this message.'
  }
});

Messages.attachSchema( MessagesSchema );
```

Up at the top we follow the same pattern as we did on our `Channels` collection. Once we have our collection defined and our allow and deny rules set, we get our schema in place. Pay close attention. While our schema isn't terribly complex in itself, the impact it will have on how we work with data later needs to be explained. At the top of our list of rules, notice that we have two fields: `channel` and `to`. What's going on here?

To avoid the need for extra collections, what we're doing here is specifying where a message belongs based on one of two criteria: is the message intended for a "public" channel or a direct message to another user? If the message is intended for a public channel, we're going to store the `_id` of that channel here in the `channel` field. If instead the message is going to another user, we're going to store it in the `to` field. The difference here is subtle, but will help us to shape our database queries later.

Beneath these two fields, we add three others: `owner`, `timestamp`, and `message`. These should be fairly self-explanatory. `owner`, here, is simply the ID of the user that is adding this message. `timestamp` is when the message was created, and message is well...the message! With all of these in place, we attach our schema to our collection and we're good to go.

Next up, let's focus on our sign up process. Our goal there will be to make sure that our user has everything they need to map to our process for wiring up direct messages later.

### Signing up users
Technically speaking, we already have the skeleton of a sign up process in place from [Base](https://github.com/themeteorchef/base/blob/4.0.0/client/templates/public/signup.html). For our needs, though, we'll need to modify this slightly to ask users for some extra information. In addition to an email address and password, we're also going to ask users for their first name, last name, and a username. In our application, this will give us the information we need to display users in the app as direct message "channels." Let's take a look at our HTML modifications and then jump into some JavaScript.

<p class="block-header">/client/templates/public/signup.html</p>

```markup
<template name="signup">
  <div class="row">
    <div class="col-xs-12 col-sm-6 col-md-4">
      <h4 class="page-header">Sign Up</h4>
      <form id="signup" class="signup">
        <div class="row">
          <div class="col-xs-6">
            <div class="form-group">
              <label for="firstName">First Name</label>
              <input type="text" name="firstName" class="form-control" placeholder="First Name">
            </div>
          </div>
          <div class="col-xs-6">
            <div class="form-group">
              <label for="lastName">Last Name</label>
              <input type="text" name="lastName" class="form-control" placeholder="Last Name">
            </div>
          </div>
        </div>
        <div class="form-group">
          <label for="username">Username</label>
          <div class="input-group username">
            <div class="input-group-addon">@</div>
            <input type="text" class="form-control" name="username" placeholder="username">
          </div>
        </div>
        <div class="form-group">
          <label for="emailAddress">Email Address</label>
          <input type="email" name="emailAddress" class="form-control" placeholder="Email Address">
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" name="password" class="form-control" placeholder="Password">
        </div>
        <div class="form-group">
          <input type="submit" class="btn btn-success" value="Sign Up">
        </div>
      </form>
      <p>Already have an account? <a href="{{pathFor 'login'}}">Log In</a>.</p>
    </div>
  </div>
</template>
```

See the new fields up top? Our first name and last name fields are pretty simple—just plain text inputs—but our username address is a bit different. What's happening there? Similar to other chat services like Slack, MegaCorp has asked that each user get an easy-to-remember username. Here, we're simply prompting users to give us a username that will exist as `@username`. Later, when the ability to reference other users via `@` is added, this will come in handy. Following along so far?

Let's get the JavaScript for this wired up. We'll need to do two things: add a safety guard for our username field (we'll see why soon) and then add support for all three fields in the actual account creation process.

#### Sanitizing usernames
Before we add our new fields to our sign up flow, we need to consider formatting of usernames. As-is, a user could technically type _anything_ they want into our `username` field. For our needs, however, we want to sanitize the passed string so that it doesn't include any unwanted characters or formatting. To make this clearer for users, what we're going to do is write a quick function to manage this sanitization for us.

Taking advantage of Meteor's support for ES2015 modules, we're going to create a new module in a separate file and then _import_ that file into our signup template's main JavaScript file.

<p class="block-header">/client/modules/sanitize-username.js</p>

```javascript
export default function( value ) {
  return value.replace( /[^A-Za-z0-9\s]/g, '' ).toLowerCase().trim();
}
```

Our module is pretty simple, just a single, one-line function! What's it doing? We're trying to do three things here:

1. Remove any punctuation from the passed value like `!@#$%`.
2. Make sure that the value is all lowercase (i.e., avoid `hEyYoUgUyS`).
3. Strip out any whitespaces converting `like this or this` to `likethisorthis`.

Inside of our `.replace()` block, we pass a regular expression which essentially says "only allow letters and numbers, exclude everything else." Notice that we keep in capital letters here as we don't want to remove those, just flatten them. Because we're adding in a `.toLowerCase()` immediately after this replace, we don't have to sweat it in our regex.

<figure>
  <img src="https://tmc-post-content.s3.amazonaws.com/2016-03-10_02:51:01:900_sanitize-demo.gif" alt="Sanitizing our string as the user types.">
  <figcaption>Sanitizing our string as the user types.</figcaption>
</figure>

This seems pretty simple...why make it a module? Good question! Though we'll only use it once in our work, when we come across features like this it's good to consider making it a module for later. In the event that we need to do this again, we know that we already have a module/function written so we can just import it where we need it. Think of this as a way to be more economical with code and save yourself trouble down the road.

Now that we have our module, we need to actually _use_ it. If we hop over to our signup template's JavaScript, we can import this and put it to use.

<p class="block-header">/client/templates/public/signup.js</p>

```javascript
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
```

Wait a minute! Hang in there, we'll explain. Up at the top, notice that we're importing our module `sanitizer-username` as the variable `sanitizeUsername` (this is possible because we only exported a single, default function from our module) and then put it to use down below. Notice that what we're doing is using this inside of a keyup event on our `username` field. What this means is that _as the user is typing_, we'll update the value of the field with the sanitized version of whatever they're typing in.

Notice that in this file, we're also adding in another module that we didn't cover. This is [coming from Base](https://github.com/themeteorchef/base/blob/4.0.0/client/modules/signup.js). To call it, notice that we're adding this to the `onRendered` block of our template. This is so that the validation inside is attached accordingly. We need to make a few modifications to this module—to account for our first name, last name, and username—so let's hop over there now.

#### Modifying our sign up flow
This will go quick. All we need to do in this file is add support for our `firstName`, `lastName`, and `username` fields in two spots: our validation and our value collection (what we pass to `Accounts.createUser()`).

<p class="block-header">/client/modules/signup.js</p>

```javascript
let template;

let _handleSignup = () => {
  let user = {
    username: template.find( '[name="username"]').value,
    email: template.find( '[name="emailAddress"]' ).value,
    password: template.find( '[name="password"]' ).value,
    profile: {
      name: {
        first: template.find( '[name="firstName"]' ).value,
        last: template.find( '[name="lastName"]' ).value
      }
    }
  };

  Accounts.createUser( user, ( error ) => {
    if ( error ) {
      Bert.alert( error.reason, 'danger' );
    } else {
      Bert.alert( 'Welcome!', 'success' );
    }
  });
};

let validation = () => {
  return {
    rules: {
      firstName: {
        required: true
      },
      lastName: {
        required: true
      },
      username: {
        required: true,
        minlength: 6,
        maxlength: 20
      },
      emailAddress: {
        required: true,
        email: true
      },
      password: {
        required: true,
        minlength: 6
      }
    },
    messages: {
      firstName: {
        required: 'What is your first name?'
      },
      lastName: {
        required: 'How about a second name?'
      },
      username: {
        required: 'What username would you like?'
      },
      emailAddress: {
        required: 'Need an email address here.',
        email: 'Is this email address legit?'
      },
      password: {
        required: 'Need a password here.',
        minlength: 'Use at least six characters, please.'
      }
    },
    errorPlacement( error, element ) {
      if ( element.attr( 'name' ) === 'username' ) {
        error.insertAfter( '.input-group.username' );
      }
    },
    submitHandler() { _handleSignup(); }
  };
};

let _validate = ( form ) => {
  $( form ).validate( validation() );
};

export default function( options ) {
  template = options.template;
  _validate( options.form );
}
```

At the very top of our file, we can see our three fields being added. First, we add in `username` in the root object and then add an additional `profile` block, setting our `firstName` and `lastName` values as `first` and `last` inside of a `name` block. The reason we're doing this is so that we can pull out user names later a little easily, accounting for the potential desire to reference users just by their first name. Neat!

If we look down in our [`validation` method](https://themeteorchef.com/snippets/validating-forms-with-jquery-validation/), we can see three additional rules being added for each of our fields. For our `username` field, notice that we're limiting users to a minimum length of 6 characters and a maximum of 20. There's not much science to this, just a way to keep usernames somewhat sensible. Before we call this complete, there's just one last thing to call attention to.

If we look down beneath the `messages` block in our validation here, we can see an additional method `errorPlacement` being added. Because we're using a [Bootstrap Input Group](http://getbootstrap.com/css/#forms-inline) to add the `@` symbol onto our field, we need to consider error placement. Here, we check to see that the field getting an error attached to it is the `username` field. If it is, we use the [jQuery Validation](https://themeteorchef.com/snippets/validating-forms-with-jquery-validation/) API to move the error to be _beneath_ the input group to prevent any weird layout quirks. 

Making progress! At this point we have our users all set for sign up. Next, we need to focus on our routing.

### Wiring up a route for channels
With users ready to be signed up, we need to get a route set up to actually send them to after they've signed up. In addition to this, we also need to adjust our permissions a bit to ensure that users are routed properly when they log in to the app. First up, let's set up a route for our chat channels.

<p class="block-header">/both/routes/authenticated.js</p>

```javascript
const authenticatedRoutes = FlowRouter.group( { name: 'authenticated' } );

authenticatedRoutes.route( '/messages/:channel', {
  name: 'channel',
  action() {
    BlazeLayout.render( 'default', { yield: 'channel' } );
  }
});
```

Inside of our [authenticated routes](https://themeteorchef.com/base/routing/) list file, here, we set up a single route. Once a user logs in, our goal will be to send them to a default channel `/messages/general`. Here, we account for this as well as the potential for other channels. When we wire up direct messages in a bit, we'll see how this also supports usernames so we can go to a route like `/messages/@username`. To make this really complete, we'll want to make a few tweaks to our permissions flow to control what a logged in user can see vs. what a logged _out_ user can see.

<p class="block-header">/client/templates/layouts/default.js</p>

```javascript
const handleRedirect = ( routes, redirect ) => {
  let currentRoute = FlowRouter.getRouteName();
  if ( routes.indexOf( currentRoute ) > -1 ) {
    FlowRouter.go( redirect );
    return true;
  }
};

Template.default.onRendered( () => {
  Tracker.autorun( () => {
    let isChannel   = FlowRouter.getParam( 'channel' ),
        bodyClasses = document.body.classList;
        
    return isChannel ? bodyClasses.add( 'is-channel' ) : bodyClasses.remove( 'is-channel' );
  });
});

Template.default.helpers({
  loggingIn() {
    return Meteor.loggingIn();
  },
  authenticated() {
    return !Meteor.loggingIn() && Meteor.user();
  },
  redirectAuthenticated() {
    return handleRedirect([
      'login',
      'signup',
      'recover-password',
      'reset-password'
    ], '/messages/general' );
  },
  redirectPublic() {
    return handleRedirect( [ 'channel' ], '/login' );
  }
});
```

Over in our main layout template's JavaScript, `default`, we need to make a few tweaks. Toward the bottom, notice that we're adjusting our redirect methods—these work in conjunction with our [layout template](https://github.com/themeteorchef/base/blob/4.0.0/client/templates/layouts/default.html)—so that if a logged in user attempts to access a _public_ route, they're redirected to `/messages/general`. If a public or logged out user attempts to access a channel, they're redirected to our `/login` route.

Up near the top of our file, we have something special going on. Because the layout of our channels template will be full-width (meaning the template will extend the full-width of the page), we need to account for moving between a channel and one of our public views (where they layout is centered in the page). To do this, here, we rely on FlowRouter's reactive `getParam()` method in conjunction with a `Tracker.autorun()` block.

What we're saying here is that when the `channel` parameter becomes visible in the URL, we want to add a class to our `<body></body>` tag called `is-channel`. If there _isn't_ a `channel` parameter, we want to remove that class from the body. This is a good place to focus next. Let's wire up the skeleton of our channel template now and start to get some data into the app.

### Adding a channel (and sidebar) template
With our routing in place, we're finally ready to dig into the templates for displaying our chat interface! We need to set up a few things below. First up is our main channel template, let's wire it up and walk through it.

<p class="block-header">/client/templates/authenticated/channel.html</p>

```markup
<template name="channel">
  {{> sidebar}}

  <div class="conversation">
    <div id="messages">
      <div class="messages-list">
        <!-- We'll output our messages here. -->
      </div>
    </div>

    <div class="message-input">
      <input name="message" placeholder="Type your message here...">
    </div>
  </div>
</template>
```

Pretty simple. Technically speaking our layout is pretty sparse, just two columns. Here, we set up our `channel` template to include both the sidebar (where we'll list all of our channels and direct messages) as well as the main list of messages for the current channel. Down below, notice that we also include our input for adding a new message here. This is where the bulk of our work will take place, so get comfy! First up, let's focus in on our sidebar.

<div class="note">
  <h3>What about CSS? <i class="fa fa-warning"></i></h3>
  <p>Because our focus is on the functionality here and not the style, we're going to skip detailing how the CSS works. If you'd like, you can find everything in the <code>/stylesheets</code> directory <a href="https://github.com/themeteorchef/building-a-chat-application/tree/master/code/client/stylesheets">over on GitHub</a>.</p>
</div>

#### Listing channels and direct messages
If we hop over to our sidebar template now, we can see that there's really not much to it. All we're doing here is outputting two lists: "Channels" and "Direct Messages." Let's take a look at the markup and then see how we populate it with data.

<p class="block-header">/client/templates/authenticated/sidebar.html</p>

```markup
<template name="sidebar">
  <aside class="sidebar">
    <h5>Channels</h5>
    <ul>
      {{#each channel in channels}}
        <li class="{{currentChannel channel.name}}">
          <a href="/messages/{{channel.name}}">
            #{{channel.name}}
          </a>
        </li>
      {{/each}}
    </ul>

    <h5>Direct Messages</h5>
    <ul>
      {{#each user in users}}
        <li class="{{currentChannel user.username}}">
          <a href="/messages/@{{user.username}}">
            {{fullName user.profile.name}}
          </a>
        </li>
      {{/each}}
    </ul>
  </aside>
</template>
```

Pretty simple. For both groups, we're simply outputting a list of `<li></li>` tags with links inside, pointing to `/messages/<channel>`. For our Channels list, notice that we use the name of the channel while our users (or Direct Messages) list is relying on the `@` symbol plus the username of the user. Making sense? On each list item, notice that we're using a helper called `currentChannel`, passing in the respective channel name or username for that item. Let's hop over to our JavaScript for this now to see how it's all working.

<p class="block-header">/client/templates/authenticated/sidebar.js</p>

```javascript
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
    let users = Meteor.users.find( { _id: { $not: Meteor.userId() } } );
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
```

A lot going on here! Let's step through it. Up top, we kick things off with a subscription to a publication called `sidebar`. Inside, we'll grab the list of channels in the application as well as all of the users in the application (who we can send direct messages to). Let's take a quick look at it.

<p class="block-header">/server/publications/sidebar.js</p>

```javascript
Meteor.publish( 'sidebar', function() {
  return [
    Channels.find(),
    Meteor.users.find( { _id: { $ne: this.userId } }, { fields: { username: 1, 'profile.name': 1 } } )
  ];
});
```

Pretty straightforward! Using Meteor's ability to [return multiple cursors from a publication](https://themeteorchef.com/snippets/publication-and-subscription-patterns/#tmc-complex-publication), we return all of the channels in our application along with all of the users. For our users, to keep things light—and secure—we exclude everything but their `_id`, `username`, and the `name` object inside of their `profile`. We also make sure to exclude the current user from the results as they won't be factored into the direct messages list (i.e., you can't message yourself). That's it! With this in place, we have all of the data we need for our sidebar.

<p class="block-header">/client/templates/authenticated/sidebar.js</p>

```javascript
[...]

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
```

Back in our sidebar JavaScript, we have a handful of helpers set up to output our data. Let's focus on the middle two. Here, we're simply grabbing all of our channels and all of our users. Our `channels()` helper can just output the data directly as no filtering is required. For our `users()` helper, however, we make sure to filter out the current user's `_id` as Meteor will automatically make them available on the client (regardless of our publication).

In tandem with this—for our users—we add in another helper `fullName()` which is responsible for converting our user's `name` object into a string like `{ first: 'Carl', last: 'Winslow' }` to `"Carl Winslow"`. We do this here as an added convenience, but you could technically just output the values in your template directly like `{{user.profile.name.first}} {{user.profile.name.last}}`. Up to you!

If we look at the very first helper, `currentChannel`, we can see how we're highlighting the selected item in the list. Remember that in our template we're passing in either the channel name or the username for each item in the list. Here, we take in that name and compare it against the current value of `channel` in our route. In other words, if we have a route like `/messages/general`, `FlowRouter.getParam( 'channel' )` will return `general`. If the name passed to our helper is _also_ general, that item will get the `active` class applied to it. If the values do not match, we simply return an empty string leaving the styles alone.

That's it! Even though we haven't set up a default channel yet, if we load up our app we should see our "Direct Messages" list being populated. Good stuff. With our sidebar in place, let's jump back up to our main channel template and see how we're outputting messages for each channel.

#### Displaying messages
Now for the fun—and mildly tricky—part! Our next task is to wire up a list of messages to display based on the selected channel. This is going to be a bit heady as there are several layers of complexity to this. Don't worry, we'll explain each but consider this your fair warning! To move forward, let's look at how we're outputting messages in our markup and then get it all connected.

<p class="block-header">/client/templates/authenticated/channel.html</p>

```markup
<template name="channel">
  {{> sidebar}}

  <div class="conversation">
    <div id="messages">
      <div class="messages-list {{#if isLoading}}loading-list{{/if}}">
        {{#each messages}}
          {{> message}}
        {{else}}
          {{#if isDirect}}
            <p class="alert alert-warning">You haven't said anything to {{username}} yet. Say hello!</p>
          {{else}}
            <p class="alert alert-warning">This channel is pretty quiet. You should way something!</p>
          {{/if}}
        {{/each}}
      </div>
    </div>

    <div class="message-input">
      <input name="message" placeholder="Type your message here...">
    </div>
  </div>
</template>
```

This is all we need. Notice that we're simply filling in our `<div class="messages-list"></div>` element here with an `{{#each}}` loop over our—eventual—`{{messages}}` helper. For each message, we output an instance of our `{{> message}}` template (we'll cover this a bit later). If we don't have any messages to display, we check whether or not the channel we're in is a direct message and display a "heads up" notification appropriate for the context. Making sense? Now for the hard work! 

Next, we need to set up our `channel` template to pull in some data. This is a bit more complex than it sounds. Remember, we need to pull in data based on the currently selected channel. Because we're dealing with two separate collections, we need to factor this in. To compensate for complexity, we're going to handle this by writing a module to help us watch for changes to the channel and parse it accordingly, grabbing the correct data in the process.

<p class="block-header">/client/templates/authenticated/channel.js</p>

```javascript
import handleChannelSwitch from '../../modules/handle-channel-switch';

Template.channel.onCreated( () => {
  let template = Template.instance();
  handleChannelSwitch( template );
});
```

The JavaScript for our `channel` template will need a bit more to be fully functional, but this is all we need to get started. Notice that here, all we're doing is using an ES2015 import statement pointing to a new module `handle-channel-switch` and assigning it to the variable `handleChannelSwitch` in our file. To use it, we call `handleChannelSwitch()` passing in the current template instance once our template is created. Crack your knuckles! Let's get this module wired up.

#### Switching channels and loading data
Pay close attention. We're going to output the entirety of our module and then step through how it's all working.

<p class="block-header">/client/modules/handle-channel-switch.js</p>

```javascript
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
```

Welp. It's not as bad as it seems. Let's take it from bottom to top. First, we call a method just above our main exported function called `_setupReactiveVariables()`, passing in our template instance. Inside, we do what it says: [create two reactive variables](https://themeteorchef.com/snippets/reactive-dict-reactive-vars-and-session-variables/#tmc-reactive-variables), `isDirect` and `loading`, setting `loading` to be `true` by default.

Next, back in our default function—and inside of a call to `Tracker.autorun()`—we make a call to another method `_handleSwitch()` a little further up in our file. Inside, we negotiate moving from one channel to another. This should look a little familiar. Just like we did in our sidebar, here, we grab the current channel parameter reactively (this is why we're wrapping our `_handleSwitch` method in an `autorun` block). If we have a channel, we determine whether or not it's a direct message by seeing if it includes an `@` symbol. If it does, we set our `isDirect` ReactiveVar accordingly and then also set our `loading` ReactiveVar to `true`.

What? Consider the flow here. Whenever our `channel` param updates in our route, we expect this code to be re-run. When it reruns, we want to make sure that we tell our template to go into a "loading" state while we move between channels (we'll wire this up to our template in a bit). This ensures that we have a smooth transition between channels, accounting for Meteor's reactivity shooting the new message data down the wire (without this it looks a bit jarring).

Finally, we hit the big monty. Inside of `_establishSubscription`, we take in our `template`, `isDirect,` and `channel` values and attempt to pull in the data for the selected channel. Review this a few times. Whenever our channel is changed, we're calling this to switch out the data source on the client. Let's jump over to the `channel` publication now to see how it negotiates all of this.

#### Wiring up our channel publication
This is a tricky one. Remember, because we're storing our messages in one collection—but loading them in different contexts—we need to account for this when we retrieve that data on the server. Here's how we're doing it:

<p class="block-header">/server/publications/channel.js</p>

```javascript
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
```

To kick things off, we make sure to [check our arguments](https://themeteorchef.com/snippets/using-the-check-package/) to ensure they're of the type we expect. Down below we get to work.

First, we check whether the current channel is considered a direct message. If it is, we grab the user that the channel name applies to (assuming it's their username). With this, we run a query on our `Messages` collection to find all of the messages where either the `owner` is the _current_ user's ID and the `to` field is the `_id` of the user passed as the channel, or, the opposite. Consider what's taking place here. Because a direct message is a conversation between two people, we need to account for the possibility that a message is either created by our current user or the person they're speaking to. 

Here, we do this by seeing whether or not the message is being sent _to_ us by the currently selected user, or, by ourselves _to_ the currently selected user. Marinate on that one for a second, it's definitely not easy to reason about the first few times looking at it! Fortunately, down below, if we're just connecting to a regular channel, we grab the channel from the passed name in our `Channels` collection and return all of the messages where the `channel` field on the message matches the passed channel's `_id`.

Phew! Pat yourself on the back. This is a _big_ chunk of work solved. There's still more to do, but now that we have data getting to the client we're in a good place. Let's keep moving.

<p class="block-header">/client/modules/handle-channel-switch.js</p>

```javascript
import setScroll from './set-scroll';

let _establishSubscription = ( template, isDirect, channel ) => {
  template.subscribe( 'channel', isDirect, channel, () => {
    setScroll( 'messages' );
    setTimeout( () => { template.loading.set( false ); }, 300 );
  });
};

[...]
```

Back in our `handle-channel-switch` module, there's just one more thing to consider. Notice that here, we're also importing _another_ module called `set-scroll`. We're calling this whenever our subscription completes (along with a call to set our `loading` ReactiveVar to `false` after a `300ms` delay). Can you guess what it's doing? Let's take a look.

<p class="block-header">/client/modules/set-scroll.js</p>

```javascript
export default function( containerId ) {
  let messages = document.getElementById( containerId );
  setTimeout( () => { messages.scrollTop = messages.scrollHeight; }, 300 );
}
```

Simple enough, but what's it doing? Here, we're taking the passed `containerId`, grabbing the matching element for it in the DOM, and setting the scroll position of that element to be the height of that element's scroll (or overflow). In other words, we're setting the scroll position to be the _bottom_ of that element. We do this here because we want to automatically scroll the user to the most recent messages in the channel which will always be at the bottom of the list. Neat, eh?

Moving right along! With this in place, we have our data source all wired up and we're ready to start getting stuff onto the template. Let's hop back up to the JavaScript for our `channel` template now and see how we're outputting data.

#### Listing messages
Now that we're pulling in the appropriate data based on the channel we're looking at, we need to actually get that data on screen. To do this, we're going to need to wire up another module. Real quick, let's see how it looks in our `channel` template and then get the module built out.

<p class="block-header">/client/templates/authenticated/channel.js</p>

```javascript
import handleChannelSwitch from '../../modules/handle-channel-switch';
import sortMessages from '../../modules/sort-messages';

[...]

Template.channel.helpers({
  isLoading() {
    return Template.instance().loading.get();
  },
  isDirect() {
    return Template.instance().isDirect.get();
  },
  username() {
    return FlowRouter.getParam( 'channel' );
  },
  messages() {
    let messages = Messages.find( {}, { sort: { timestamp: 1 } } );
    if ( messages ) {
      return sortMessages( messages );
    }
  }
});
```

The part we want to pay attention to is the `messages()` helper. Notice that the other helpers here are pretty minimal, just pulling in the values of our ReactiveVar's or—when applicable—the username we're messaging. We won't cover these directly, so make sure to review how they're positioned in the template markup to get a feel for how they're working.

So, messages. Technically speaking, we could just output our list of messages here and be done with it. To add a bit of polish, though, we want to output our messages so that they only show the user's name (and the timestamp) for the message if that message was added more than five minutes ago. This is similar to how Slack groups messages in their own threads. 

We've already imported our module as `sortMessages` up top. In our helper, we simply call to our `Messages` collection to retrieve our data and if we get it, pass it into the invocation of our module. Let's jump over there now and see how we're outputting each of our messages.

<p class="block-header">/client/modules/sort-messages.js</p>

```javascript
let _getTimeDifference = ( previousTime, currentTime ) => {
  let previous   = moment( previousTime ),
      current    = moment( currentTime );
  return moment( current ).diff( previous, 'minutes' );
}

let _checkIfOwner = ( previousMessage, message ) => {
  return typeof previousMessage !== 'undefined' && previousMessage.owner === message.owner;
};

let _decideIfShowHeader = ( previousMessage, message ) => {
  if ( _checkIfOwner( previousMessage, message ) ) {
    message.showHeader = _getTimeDifference( previousMessage.timestamp, message.timestamp ) >= 5;
  } else {
    message.showHeader = true;
  }
};

let _mapMessages = ( messages ) => {
  let previousMessage;
  return messages.map( ( message ) => {
    _decideIfShowHeader( previousMessage, message );
    previousMessage = message;
    return message;
  });
};

export default function( messages ) {
  return _mapMessages( messages );
}
```

Okay. Starting at the bottom, we simply make a call to an internal function `_mapMessages()`, passing in our cursor from our helper. Inside, we set up a placeholder variable `previousMessage` to store the _last_ message we looped over in our `.map()` method. Slow down and consider what that means. Here, our goal is to figure out how we should display the next message in the list. If the one _before it_ was added more than five minutes ago, we want to ensure that the current message being looped over has the user's name and the timestamp of that message displayed. Otherwise, we want to omit it.

For each message we loop over, we decide whether or not we should show the header in `decideIfShowHeader()`, comparing two things: whether or not the message is owned by the same person as the previous message and whether or not the timestamp of those two messages are greater than five minutes apart. Woof. Step through that. Because we're running in a `.map()` which loops overy every message passed in, we're effectively saying "tell us if we should display the header for this message" based on the rules above. Cool?

To make sense of how this is working, let's take a peek at our `{{> message}}` template quick. Remember, for each message returned by our `messages()` helper, we're returning an instance of this template.

<p class="block-header">/client/templates/message.html</p>

```markup
<template name="message">
  <div class="message">
    {{#if showHeader}}
      <header>
        <h4>{{name owner}} <span>{{messageTimestamp timestamp}}</span></h4>
      </header>
    {{/if}}

    <div class="body">
      {{{parseMarkdown message}}}
    </div>
  </div>
</template>
```

Ah, ha! See how it's coming together? If our module flags our message as needing a header, here, we reveal the name and timestamp of that message. The `{{name}}` helper in use here is simply combining the name of our user object into a string (remember, we store first and last names separately). `{{messageTimestamp}}` on the other hand is taking in the passed time and deciding whether to show the time as just the hours and minutes (meaning the messages was posted today), or the hours and minutes along with the month, day, and year of the message. Here's the meat of it:

<p class="block-header">/client/helpers/template/date-time.js</p>

```javascript
Template.registerHelper( 'messageTimestamp', ( timestamp ) => {
  if ( timestamp ) {
    let today         = moment().format( 'YYYY-MM-DD' ),
        datestamp     = moment( timestamp ).format( 'YYYY-MM-DD' ),
        isBeforeToday = moment( today ).isAfter( datestamp ),
        format        = isBeforeToday ? 'MMMM Do, YYYY hh:mm a' : 'hh:mm a';
    return moment( timestamp ).format( format );
  }
});
```

So far so good? Cool. Let's keep chuggin'. This is an EXCELLENT point to be at. At this point, we've got our messages list all wired up _and_ that messages list is all set up to update when we change channels. To wrap this thing up and call it a finished product, we need to wire up the ability to actually insert messages into our app. To the Batmobile!

### Inserting messages
We've come a long way. At this point we're all set up to display messages on a per-channel basis, but now we have one last major task: actually _adding_ messages. The good news is that our work now should be well-informed by what we've accomplished above. Let's add an event for our `message` input in our `channel` template to kick things off.

<p class="block-header">/client/templates/authenticated/channel.js</p>

```javascript
[...]
import handleMessageInsert from '../../modules/handle-message-insert';

[...]

Template.channel.events({
  'keyup [name="message"]' ( event, template ) {
    handleMessageInsert( event, template );
  }
});
```

Are you kidding me?! Nope. This is good for your health, I promise. Same steps as before, we're going to stash our process into a module to keep our code neat and tidy. The only thing to note here is that we're firing this module whenever a keyup event occurs in our `message` input (meaning a user is typing). We'll negotiate _when_ to actually insert a message inside of the module. Fall in line, cadet!

<p class="block-header">/client/modules/handle-message-insert.js</p>

```javascript
import setScroll from './set-scroll';

let _getMessage = ( template ) => {
  let message = template.find( '[name="message"]' ).value;
  return message.trim();
};

let _checkIfCanInsert = ( message, event ) => {
  return message !== '' && event.keyCode === 13;
};

let _buildMessage = ( template ) => {
  return {
    destination: FlowRouter.getParam( 'channel' ).replace( '@', '' ),
    isDirect: template.isDirect.get(),
    message: template.find( '[name="message"]' ).value
  };
};

let _handleInsert = ( message, event, template ) => {
  Meteor.call( 'insertMessage', message, ( error ) => {
    if ( error ) {
      Bert.alert( error.reason, 'danger' );
    } else {
      event.target.value = '';
    }
  });
};

export default function( event, template ) {
  let text      = _getMessage( template ),
      canInsert = _checkIfCanInsert( text, event );

  if ( canInsert ) {
    setScroll( 'messages' );
    _handleInsert( _buildMessage( template ), event, template );
  }
}
```

Oh boy. Lots going on here but the same thought process applies. Whenever our user is typing in the input, we call this module up. First up, we grab the current value of the input and `trim()` off any whitespace to make sure the user hasn't left their head on the keyboard. Next, we check if they can insert the message by checking two things: whether or not the current value is empty (if this was blank spaces, our `trim()` would give us an empty string) and then we verify that the key they just pressed was the enter key (designated as the number `13` in robot speak). 

If both tests pass, we say that they _can_ insert the message. Reprising our `setScroll()` module from earlier, we call this to pre-emptively scroll our messages list to the bottom. This works here as, oddly enough, our message will hit the server—and by proxy our publication—before our method call to the server fires its callback. Weird, but hey! Next up, we handle our insert by calling to build our message—we just put together an object of the necessary values including our channel name, whether the message is direct, and the text we grabbed earlier—and then passing everything to our method call on the server.

We're super close! Note: If the message comes back as inserted okay, we go ahead and wipe out our input. Let's close this loop and hop up to the server for cocktails and refreshments!

#### Wiring up our insert on the server
Just to drive you crazy, this will be a two-parter. First up, let's take a look at our method and then see how we're actually getting the message into our `Messages` collection.

<p class="block-header">/server/methods/insert/messages.js</p>

```javascript
import insertMessage from '../../modules/insert-message';

Meteor.methods({
  insertMessage( message ) {
    check( message, {
      destination: String,
      isDirect: Boolean,
      message: String
    });

    try {
      insertMessage( message );
    } catch ( exception ) {
      throw new Meteor.Error( '500', `${ exception }` );
    }
  }
});
```

Haha. Yeah, modules. Before we call up our module, though, we pull in our `message` argument and check it for the correct types. If all is good, we go ahead and call our freshly imported `insertMessage` module to finish the job.

<p class="block-header">/server/modules/insert-message.js</p>

```javascript
let _insertMessage = ( message ) => {
  return Messages.insert( message );
};

let _escapeUnwantedMarkdown = ( message ) => {
  // Escape h1-h6 tags and inline images ![]() in Markdown.
  return message
  .replace( /#/g, '&#35;' )
  .replace( /(!\[.*?\]\()(.*?)(\))+/g, '&#33;&#91;&#93;&#40;&#41;' );
};

let _cleanUpMessageBeforeInsert = ( message ) => {
  delete message.destination;
  delete message.isDirect;
  message.message = _escapeUnwantedMarkdown( message.message );
};

let _getChannelId = ( channelName ) => {
  let channel = Channels.findOne( { name: channelName } );
  if ( channel ) {
    return channel._id;
  }
};

let _getUserId = ( username ) => {
  let user = Meteor.users.findOne( { username: username } );
  if ( user ) {
    return user._id;
  }
};

let _assignDestination = ( message ) => {
  if ( message.isDirect ) {
    message.to = _getUserId( message.destination );
  } else {
    let channelId = _getChannelId( message.destination );
    message.channel = channelId;
  }
};

let _checkIfSelf = ( { destination, owner } ) => {
  return destination === owner;
};

let _assignOwnerAndTimestamp = ( message ) => {
  message.owner     = Meteor.userId();
  message.timestamp = new Date();
};

export default function( message ) {
  _assignOwnerAndTimestamp( message );

  if ( !_checkIfSelf( message ) ) {
    _assignDestination( message );
    _cleanUpMessageBeforeInsert( message );
    _insertMessage( message );
  } else {
    throw new Meteor.Error( '500', 'Can\'t send messages to yourself.' );
  }
}
```

Hey, wait! Don't leave! Just like our other modules, this may look scary but all we're doing is breaking up an otherwise complex task into easy to digest steps. That's it. Let's step through it.

Before we get into it, we quickly assign the owner of the message (the currently logged in user) and a timestamp for when the message is being inserted. Next, passing this freshly modified message to our `_checkIfSelf()` method to ensure that our user isn't being a trickster and trying to send themselves a message. If not, we determine where to route the message by testing whether or not the message is direct or public.

Depending on the result, we grab the appropriate channel or userId to route the message to in `_getChannelId()` and `getUserId()` (respectively) and assign them to the message. Finally, before we insert the message, we "clean it up," meaning we escape the unwanted Markdown tags in the message. Remember, MegaCorp wants to be similar to Slack in that h1-h6 tags and inline images are _not_ converted to HTML. Here, we run a series of regular expressions to find the appropriate elements and convert them into their ASCII code representation (which our Markdown parser will simply convert to a character, not parse itself).

Once we have a our message...we insert it! Donezo. If all went well in this process, our user should see their message show up in the current channel. How cool is that? We're not _quite_ done. Technically we're all set up, but remember that MegaCorp wants to ensure that at least one default public channel exists. Let's get that set up now.

### Adding default data
This is really quick. Before we can call it a day, we need to [seed our database](https://themeteorchef.com/recipes/writing-a-database-seeder/) with a default `#general` channel. We'll do this in two quick steps, relying on the new [themeteorchef:seeder](https://atmospherejs.com/themeteorchef/seeder) package included in [Base v4.0.0](https://github.com/themeteorchef/base/tree/4.0.0) to get the job done.

<p class="block-header">/server/startup.js</p>

```javascript
import seedDatabase from './modules/seed-database';

Meteor.startup( () => {
  seedDatabase();
});
```

To run the seeding process, we import a module `seed-database` and call it when our server starts up. The module itself is really simple:

<p class="block-header">/server/modules/seed-database.js</p>

```javascript
import seed from 'meteor/themeteorchef:seeder';

let _seedChannels = () => {
  seed( 'channels', {
    environments: [ 'development', 'staging', 'production' ],
    data: [ { name: 'general' } ]
  });
};

export default function() {
  _seedChannels();
}
```

That's it! Using the `seed()` method, we pass in the name of the collection we want to seed—in this case our `Channels` collection—along with the environments we want the data to be created in and the data we want to insert into the collection. Because our `Channels` schema is pretty simple, all we need to do here is pass the name of the channel and we're done!

<figure>
  <img src="https://tmc-post-content.s3.amazonaws.com/2016-03-10_06:41:21:006_megacorp-chat-demo.gif" alt="The MegaCorp folks are going to love this.">
  <figcaption>The MegaCorp folks are going to love this.</figcaption>
</figure>

Looks great. Nice work! Now to pitch some improvements...

### Wrap up & summary
In this recipe, we learned how to build a simple, real-time chat app. We learned how to store our data efficiently when handling messages, as well as how to retrieve that data without a lot of headaches. We also learned how to use Markdown, and control what gets rendered _in_ that Markdown by escaping our strings with regular expressions.
