# ClassyShark - Get started

Let's examine the following simplified scenario. What if we have a main method that calls a FancyLibrary (simulated dependency). FancyLibrary is a simple Reader/Writer implementation, where a field is written once and accessed many times. Our app is using FancyLibrary, as a 3-rd party dependency. Here is our app's main code:

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
* thread T4 ==> read value
* thread T5 ==> read value
* thread T6 ==> read value

Let's say FancyLibrary is a popular open source project, so to find the problem we need to research the code and make sure we have an understanding, which code made its way to the jar in our Android project.

Let's use ClassyShark, first let's fire up ClassyShark

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Get%20Started%20Open%20File.png)


Double Click, opening the FancyLib.jar

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Get%20Started%20Lib%20View.png)

We see, all the classes exactly as our app sees them. Let's head into FancyLibrary.class, as this is the class from our code that does the trouble (either in command line or double click)

![](https://github.com/googlesamples/android-classyshark/blob/master/Resources/Get%20Started%20Class%20View.png)

Now we see the problem, this class is not thread safe because of the variable declaration. One way to solve this is to wrap the access to the FancyLibrary inside synchronized block.

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
Bottom line the FancyLib is not thread safe, however it was not documented anywhere.
