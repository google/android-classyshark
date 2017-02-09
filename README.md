# ClassyShark

### Introduction

Follow the shark on twitter [@ItsClassyshark](https://twitter.com/ItsClassyshark)

![alt text](https://github.com/borisf/classyshark-user-guide/blob/master/images/5%20ClassesDexData.png)

ClassyShark is a standalone binary inspection tool for Android developers. It can reliably browse any Android executable and show important info such as class interfaces and members, dex counts and dependencies. ClassyShark supports multiple formats including libraries (.dex, .aar, .so), executables (.apk, .jar, .class) and all Android binary XMLs: AndroidManifest, resources, layouts etc.

### Useful links
* [User guide] (https://github.com/borisf/classyshark-user-guide)
* [Command-line reference] (https://github.com/google/android-classyshark/blob/master/CommandLine.pdf)
* Gradle [sample](https://github.com/google/android-classyshark/tree/master/Samples/SampleGradle)
* [Roadmap](https://docs.google.com/document/d/1sK_WNzHn_6Q1V_dohxrtk1tlsPXsi9cEVnIuYuVig0M/edit?usp=sharing)

### Download
To run, grab the [latest JAR](https://github.com/google/android-classyshark/releases)
and run `java -jar ClassyShark.jar`.

### Develop
1. Clone the repo
2. Open in your favorite IDE/editor
3. Build options:
     * IntelliJ - builds automatically when exporting the project 
     * [Gradle script](https://github.com/google/android-classyshark/blob/master/ClassySharkWS/build.gradle)
     * [Retrobuid](https://github.com/borisf/RetroBuild)

### Arch Linux

If you're running Arch Linux you can install the latest [prebuilt jar from the AUR](https://aur.archlinux.org/packages/classyshark/).

### Dependencies
* [dexlib2](https://github.com/JesusFreke/smali/tree/master/dexlib2) by jesusfreke
* [guava](https://github.com/google/guava) by Google
* [ASM](http://asm.ow2.org/) by OW2
* [ASMDEX](http://asm.ow2.org/asmdex-index.html) by OW2
* [java-binutils](https://github.com/jawi/java-binutils) by jawi
* [BCEL](https://commons.apache.org/proper/commons-bcel) by Apache

### Support
If you've found an error, please file an issue:

https://github.com/google/android-classyshark/issues

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub.

License
=======

    Copyright 2016 Google, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



