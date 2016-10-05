# FollowYourFriends
A simple app I made for an assignment and to teach myself how to make android apps

It was a fun project for me to teach myself how to do Android apps. I won't be making any more updates, and I present this to you as-is, but feel free to take a look, any feedback you might have is always welcome. This repo includes the files for the app itself (uses gradle) and the server needed to run the app (uses SBT) which can be run by using [`activator`] (https://www.playframework.com/documentation/2.5.x/JavaDatabase). One word of warning though: this app is incredibly unsecure. I do no db input sanitazation, everything is communicated in plaintext etc. so if you use your real credentials it will be open for everybody to see. 

The app works as follows:
- You log in or register using your "phone number" (it will accept any number submited such as `123`).
- You send a request to a friend which has also registered
- When they accept your request you can see their location on the map from the main screen
- You can also delete friends from the menu

Copyright (c) 2016 Daniel Vente

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
