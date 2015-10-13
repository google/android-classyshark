# ClassyShark - Android executables browser

### This is not an offical Google product

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Intro.png)

While developing apps we tend to think about dexs, jars, apks and classes as build/run time abstractions that just work. Most of the time this is true, however, when trying to debug runtime issues in large apps, things can sometimes get tricky.

After working with 3rd party dependencies and analysing crashes neither being able to view the sources nor having time time to learn the library, I wrote a tool to assist me. By looking inside the jars and dexes I can see how they work and which libraries they call. 

With ClassyShark analyzing dependencies is breeze. ClassyShark is fast and shows the right info (dependency classes,packages, methods and AndroidManifest) in no time. If your app is crashing or misbehaving at runtime and you’re not sure why then ClassyShark may be able to help you.

Hope you find ClassyShark as useful and enjoyable as I do. Get started [here] (https://github.com/googlesamples/android-classyshark/blob/master/Get%20Started.md)

## Features
* Multi tabbed interface
* Apk/Dex/Jar/Class formats
* Android manifest
* Incremental/Camel/Fuzzy searches

## Dependencies
* dexlib2 by jesusfreke
* guava bu Google

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



