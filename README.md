# NBA Android App
[![Release status](https://img.shields.io/badge/release-v0.4.0--alpha-red.svg)](https://github.com/jorgegil96/All-NBA/releases)

Android app for NBA fans. Features live scores, highlights, alerts and Reddit integration for discussions.

## Features
* Real-time scores and box scores
* [/r/NBA](https://www.reddit.com/r/nba) live and post game discussion threads
* [/r/NBA](https://www.reddit.com/r/nba) and all team subreddits threads
* Push notifications when games are close/interesting
* Push notifications when games begin/end
* User-submitted highlight videos
* Standings

## Screenshots
<img src="art/games.png" alt="Ready" width="180px;"/>
<img src="art/game_threads.png" alt="Ready" width="180px;"/>
<img src="art/reddit_feed.png" alt="Ready" width="180px;"/>
<img src="art/submission.png" alt="Ready" width="180px;"/>

[More screenshots](http://imgur.com/a/4h75K)

## Downloads

A public Play Store version will not be available until a stable release is out, but there is an alpha version published, [fill out this form to get an invitation](https://goo.gl/forms/vqZOBjkvZjxGOy0k2).

Pre-release APK is available [here](https://github.com/jorgegil96/All-NBA/releases).

## Contributing  

Fix a bug, add a new feature or make the code prettier!

### Steps to contribute:
1. Fork the repository
2. Look at the list of [open issues](https://github.com/jorgegil96/All-NBA/issues), pick one or create a new one (this can be for a bug or wanted feature)
3. Write code and document it, look at the code around you and try to follow the style and conventions used. The MVP design pattern is used throughout the app.
4. Write unit tests (if possible)
5. Run ALL tests by executing `./gradlew test`
6. Send a pull request

Some resources and libraries that you might find useful:
* [JRAW](https://github.com/thatJavaNerd/JRAW): the Java Reddit API Wrapper, used throughout the application for integrating any reddit functionalities   
* [RxJava 2](https://realm.io/news/gotocph-jake-wharton-exploring-rxjava2-android/): A Java implementation of ReactiveX, a library for composing asynchronous and event-based programs by using observable sequences  
* [Retrofit 2](https://square.github.io/retrofit/): A type-safe HTTP client for Android and Java  
* [Butter Knife](http://jakewharton.github.io/butterknife/): Field and method binding for Android views  
* [Picasso](http://square.github.io/picasso/): A powerful image downloading and caching library for Android  
* [Material Dialogs](https://github.com/afollestad/material-dialogs): A beautiful, fluid, and customizable dialogs API

### What if I don't code?  

You can contribute by reporting bugs, suggesting new features or translating the app!
