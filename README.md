# FollowYourFriends
A simple app I made for an assignment and to teach myself how to make android apps

It was a fun project for me to teach myself how to do Android apps. I won't be making any more updates, and I present this to you as-is, but feel free to take a look, any feedback you might have is always welcome. This repo includes the files for the app itself (uses gradle) and the server needed to run the app (uses SBT) which can be run by using [`activator`] (https://www.playframework.com/documentation/2.5.x/JavaDatabase). One word of warning though: this app is incredibly unsecure. I do no db input sanitazation, everything is communicated in plaintext etc. so if you use your real credentials it will be open for everybody to see. 

The app works as follows:
- You log in or register using your "phone number" (it will accept any number submited such as `123`).
- You send a request to a friend which has also registered
- When they accept your request you can see their location on the map from the main screen
- You can also delete friends from the menu
