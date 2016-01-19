# ClassyShark

This is not an official Google product

## Introduction

ClassyShark is a standalone tool for Android developers. It can reliably browse any Android executable and show important info such as class interfaces and members, dex counts and dependencies. The browser supports multiple formats including libraries (.dex, .aar, .so), executables (.apk, .jar, .class) and AndroidManifest (.xml).

![Screen Flow Demo](https://github.com/google/android-classyshark/blob/master/Resources/Intro.png)

## Use cases

### Apk Content
Browse all Android & Java executable files both as standalone or as part of your APK  

* Verify that the business crucial data is properly obfuscated
* Identify all your app dependencies, both Java and native

### Multidex
Understand the contents of [multidex](http://developer.android.com/tools/building/multidex.html) apps

* Show the content of each classes.dex file
* Show the dex counts for packages, methods, classes, strings etc
* Show method count for jars (to measure the dex cost of that jar)

### JNI & Native Code
Troublshoot JNI problems including: 

* Identifying missing native libraries
* Browsing native methods per classes.dex

Troublshoot native code problems including:
* Checking native depedendencies 
* Checking dynamic symbols


## Command line 
1. Export all generated data to a text file
`java -jar ClassyShark.jar -dump <BINARY_FILE>`
2. Export generated file from a specific class to a text file
`java -jar ClassyShark.jar -dump <BINARY_FILE> <FULLY_QUALIFIED_CLASS_NAME>`
3. Open ClassyShark and display a specific class in the GUI
`java -jar ClassyShark.jar -open <BINARY_FILE> <FULLY_QUALIFIED_CLASS_NAME>`
4. Inspect APK
`java -jar ClassyShark.jar -inspect <YOUR_APK.apk>`

## Downloads

To run, grab the latest jar from [here] (https://github.com/google/android-classyshark/releases)
and type `java -jar ClassyShark.jar`.

## Start developing
### Android

Clone this repository and import the `ClassySharkAndroid` folder in Android Studio.

### Desktop

Clone this repository and import the `ClassySharkWS` folder in your favorite IDE. For releases  [RetroBuild](https://github.com/borisf/RetroBuild), a fast jar build system is used.

## Dependencies
* [dexlib2](https://github.com/JesusFreke/smali/tree/master/dexlib2) by jesusfreke
* [guava](https://github.com/google/guava) by Google
* [ASM](http://asm.ow2.org/) by OW2
* [ASMDEX](http://asm.ow2.org/asmdex-index.html) by OW2
* [java-binutils](https://github.com/jawi/java-binutils) by jawi
* [BCEL](https://commons.apache.org/proper/commons-bcel) by Apache

## Support
If you've found an error, please file an issue:

https://github.com/google/android-classyshark/issues

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



