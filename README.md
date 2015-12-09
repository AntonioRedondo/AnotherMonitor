# AnotherMonitor

<img align="center" src="https://lh4.ggpht.com/gfwMh4Ih0VD0AaxI8_eh11m6CRu_zSW6-U6F25AjCdlUjCkliWHBgJMhDb3ePdl_EMoT" width="180px" height="300px" />
<img align="center" src="https://lh4.ggpht.com/fugTTF9i76nsfnpWfv34xe1Xz5u4dDWOqbTYkBaPrzud4zPuYIZtQQhEyH7pX9POjYU" width="180px" height="300px" />
<img align="center" src="https://lh5.ggpht.com/96BmklbBOEOgL5mmXZQkofwswLGEzY4Zf6EirtF2nOBgf_cTo86RxuzCInv7etIfNgTO" width="180px" height="300px" />

AnotherMonitor monitors and records the CPU and memory usage of Android devices.

## Download ready-to-use app

The app is released on Google Play: https://play.google.com/store/apps/details?id=org.anothermonitor.

## Details

AnotherMonitor is ideal to find out the device CPU and memory resources state in real time.

It has 2 main options:
- It shows a graphic and several text labels wherein the values of the CPU and memory usage are updated every 0.5, 1, 2 or 4 seconds.
- It can record on a CSV file the read values for a later usage and process on a spreadsheet program.

The app can run in the background. Then, the second option is specially interesting since, in the background, AnotherMonitor consumes little resources and can monitor and record the CPU and memory values that other applications are using in the foreground.

The app can be closed, and the recording of values can be started from the buttons shown in the system bar.

In order to get the CPU usage the app does not make use of the `Top` command from Linux but instead it parses the `/proc/stat` file and work out the calculatios with the user and system time.

#### Retriving device processes since Android 5.1.1+

For devices running Android 5.1.1+ it is not possible any more to retrieve the processes list with [ActivityManager.getRunningAppProcesses()](http://developer.android.com/reference/android/app/ActivityManager.html#getRunningAppProcesses%28%29). This change has not been documented anywhere on the official documentation nor an alternative way to retrieve the list has been provided. Thankfully the community at Stack Overflow came out with some [satisfying solution](http://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag). For devices with Android 5.1.1+ AnotherMonitor now uses the unofficial [AndroidProcesses](https://github.com/jaredrummler/AndroidProcesses) library to retrieve the processes list. The drawback of this is that for some processes there is no way to retrieve the app name nor the icon and instead only the long package name is shown.

## Resolving dependencies

AnotherMonitor makes use of some external libraries to carry out different tasks:
-  [Android v4 Support Libraries](http://developer.android.com/tools/support-library/features.html#v4).
-  [AndroidProcesses](https://github.com/jaredrummler/AndroidProcesses): retrieves the device processes list to populate the 'Processes' screen.

These all dependencies are already included and configured on the Gradle files.

## Known issues

- [Issue#1](https://github.com/AntonioRedondo/AnotherMonitor/issues/1): On some devices the graph is not shown.

## Contribute!

If you have an awesome pull request no matter whether it's a big or small change, send it over! Your link to your GitHub account will be added below.

## License

AnotherMonitor makes use of the [GNU GPL v3.0](http://choosealicense.com/licenses/gpl-3.0/) license. Remember to make public your project source code when reusing AnotherMonitor code.
