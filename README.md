# DrawTogether
![Screenshot](/screenshots/device-2016-07-06-211145.png?raw=true "Screenshot")

The project DrawTogether is about drawing an image in a small group (max 4 players) within a given time.
The player can create a new game, choose the count of rounds and set the time border. He is also able to join an already running game.
The idea is to draw a nice picture in the group and to get the most likes from other users.

### [Demo Video](https://www.youtube.com/watch?v=Qhg0mRFMkH8)

## Getting Started

### Prerequisities

* The Java 7 and latest development kit should be installed.
* For the easy setup we recommend to install IntelliJ IDEA or Android Studio.
* Installed SBT (http://www.scala-sbt.org/) on your PC.


### Installation

At first you need to configure your environment to get application running
 
**The system variables**

```
ANDROID_HOME = C:\YourPath\Android\sdk
JAVA_HOME = C:\YourPath\Java\jdk1.7.latest
```

**Android Studio or IntelliJ IDEA**

* Install the Scala plugin : Tools-> Android -> SDK Manager -> Plugins
* Make sure you have installed the sbt compiler as well
* Project structure settings:
    *  Project SDK 1.7 java
    *  Modules
        * server 1.7 java version
        * shared 1.7 java version
        * android API 23 Platform
        * check out the project from the github repository

## Deployment

For all modules you have to built the application with sbt

```
> cd C:\YourProjectPath\DrawTogether
> sbt
```

**Server**
```
> project server
> run
```

Check if the server is running: the link http://localhost:9000 should be available in your device browser 


To access the dev-database run
```
> h2-browser
```

To update the data model run
```
> gen-tables
```

**Client**

Connect your Android phone in usb debugging mode and execute:
```
> project android
> run
```

To forward the ports form the localhost server to the client you can use the following url in Chrome:

```
chrome://inspect/#devices
```

## Authors
* **Starofall** 
* **Kalasouskaya** 

