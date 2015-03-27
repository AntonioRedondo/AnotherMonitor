# AnotherMonitor

<img align="center" src="https://lh4.ggpht.com/gfwMh4Ih0VD0AaxI8_eh11m6CRu_zSW6-U6F25AjCdlUjCkliWHBgJMhDb3ePdl_EMoT" width="180px" height="300px" />
<img align="center" src="https://lh4.ggpht.com/fugTTF9i76nsfnpWfv34xe1Xz5u4dDWOqbTYkBaPrzud4zPuYIZtQQhEyH7pX9POjYU" width="180px" height="300px" />
<img align="center" src="https://lh5.ggpht.com/96BmklbBOEOgL5mmXZQkofwswLGEzY4Zf6EirtF2nOBgf_cTo86RxuzCInv7etIfNgTO" width="180px" height="300px" />

AnotherMonitor monitors and records the CPU and memory usage of Android devices.

## Download ready-to-use app

[![App Icon](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=org.anothermonitor)

The app is released on Google Play: https://play.google.com/store/apps/details?id=org.anothermonitor.

## Details

AnotherMonitor is intended for developers interested on knowing the device resources state.

It has 2 main options:
- It shows a graphic and several text labels wherein the values of the CPU usage and memory are updated in real time;
- It can record in a CSV file the read values for a later usage and process in a spreadsheet program.

The app can be run in background. Then, the second option is specially interesting since, in background, AnotherMonitor consumes little resources and can monitor and record the memory and CPU usage values that other applications on foreground are using.

In order to get CPU usage the app does not make use of the 'Top' command from Linux but instead it parses the '/proc/stat' file and does calculations with the user and system time.

## Resolving dependencies

AnotherMonitor makes use of 3rd party libraries to carry out different tasks:
-  [Android v4 Support Libraries](http://developer.android.com/tools/support-library/features.html#v4).

These all dependencies are already included and configured on the Gradle files.

## License

AnotherMonitor makes use of the [GNU GPL v3.0](http://choosealicense.com/licenses/gpl-3.0/) license. Remember to make public your project source code when reusing AnotherMonitor code.
