# ClassyShark

This is not an official Google product

## Introduction

ClassyShark is a handy browser for Android executables. It has clients for both Android (apk) and Desktop (jar). With ClassyShark you can open APK/Zip/Class/Jar files and analyze their contents.

![Screen Flow Demo](https://github.com/google/android-classyshark/blob/master/Resources/ClassySharkAnimated.gif)

## Use cases

### Multidex
Helps you understand the contents of [multidex](http://developer.android.com/tools/building/multidex.html) apps

* Show the content of each classes.dex file
* Show the dex counts for methods, classes, strings etc

### Performance
Identify and understand performance problems including: 

* Slow libraries
* Duplicate libraries
* Redundant libraries

### Security
Identify security risks including: 

* AndroidManifest inside production APK
* Verifying obfuscated code

### Debugging
Helps you debug your app by allowing you to:

* Show class name collisions 
* Show missing and/or changed code due to Proguard obfuscation
* Trace method calls in obfuscated APK

## Downloads

Grab the latest release from [here] (https://github.com/googlesamples/android-classyshark/releases).

## Start developing
Clone this repository and import the `ClassySharkWS` folder in your favorite IDE.

## Dependencies
* [dexlib2](https://github.com/JesusFreke/smali/tree/master/dexlib2) by jesusfreke
* [guava](https://github.com/google/guava) by Google
* [ASM](http://asm.ow2.org/) by OW2

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



