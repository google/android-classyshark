# ClassyShark

### This is not an official Google product

## Introduction

ClassyShark is a handy Android executables browser, having both Android and Desktop client. With ClassyShark you can open any APK/Zip/Class/Jar file and analyze the contents.

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Intro.png)

## Use cases

1. Multidex analysis (content of each classes.dex and dex limits)
2. Dependency analysis
3. Class name collisions
4. Missing dependencies
5. Code size analysis
6. Discover Android permissions for 3-rd party libraries
7. Checking that proguard didn't strip usefull code
8. Stubs generation for testing

## Downloads

Grab the latest release from [here] (https://github.com/googlesamples/android-classyshark/releases).

## Sample problem

What if we have a main method that calls a FancyLibrary (simulated dependency). FancyLibrary is a simple Reader/Writer implementation, where a field is written once and accessed many times. Here is our app's main code:

``` java
final FancyLibrary fancyLibrary = new FancyLibrary();

for (int i = 0; i < 50; i++) {
   new Thread(new Runnable() {
       @Override
       public void run() {
           fancyLibrary.changeNumber();
       }
   }, "T" + i).start();
}
```

The full example is [here](https://github.com/googlesamples/android-classyshark/tree/master/Scenarios).

When running this code we have a race condition, in FancyLibrary more than one thread changes the value:

* thread T0 ==> wrote value
* thread T1 ==> wrote value
* thread T2 ==> read value
* thread T3 ==> read value
* ...

FancyLibrary is a popular open source project, so to find the problem we need to research the code and make sure we have an understanding, which code made its way to the jar in our Android project.

Let's use ClassyShark, first let's fire up ClassyShark

Double Click, opening the FancyLib.jar

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Get%20Started%20Lib%20View.png)

We see, all the classes exactly as our app sees them. Let's head into FancyLibrary.class, as this is the class from our code that does the trouble (either in command line or double click)

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Get%20Started%20Class%20View.png)

Now we see the problem, this class is possibly not thread safe because of the variable declaration. One way to solve this is to wrap the access to the FancyLibrary inside synchronized block.

``` java
final FancyLibrary fancyLibrary = new FancyLibrary();

   for (int i = 0; i < 50; i++) {
       new Thread(new Runnable() {
           @Override
           public void run() {
          
               wrapLibrary(fancyLibrary);
           }
       }, "T" + i).start();
   }

private static synchronized void wrapLibrary(FancyLibrary fLib) {
   fLib.changeNumber();
}
```
Bottom line the FancyLib is possibly not thread safe, however it was not documented anywhere.

## Start developing
Clone this repository and import the `ClassySharkWS` folder in your favorite IDE.

## Dependencies
* dexlib2 by jesusfreke
* guava by Google
* ASM by OW2

## Support
If you've found an error, please file an issue:

https://github.com/googlesamples/android-classyshark/issues

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub.

License
=======

    Copyright 2015 Google, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



