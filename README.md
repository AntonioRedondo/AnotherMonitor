# AnotherMonitor

.
<img src="https://lh4.ggpht.com/gfwMh4Ih0VD0AaxI8_eh11m6CRu_zSW6-U6F25AjCdlUjCkliWHBgJMhDb3ePdl_EMoT" width="180px" />
<img src="https://lh4.ggpht.com/fugTTF9i76nsfnpWfv34xe1Xz5u4dDWOqbTYkBaPrzud4zPuYIZtQQhEyH7pX9POjYU" width="180px" />
<img src="https://lh5.ggpht.com/96BmklbBOEOgL5mmXZQkofwswLGEzY4Zf6EirtF2nOBgf_cTo86RxuzCInv7etIfNgTO" width="180px" />

AnotherMonitor monitors and records the CPU and memory usage of Android devices.

## Download ready-to-use app

The app is released on Google Play: https://play.google.com/store/apps/details?id=org.anothermonitor.

## IMPORTANT NOTICE for Android 7.0 and Android 8.0 devices

Due to undocumented changes made by Google on Android 7.0 CPU usage information for processes others than the own AnotherMonitor will not work unless you have root access. Rest of the app will work as usual. For devices with Android 8.0 the AnotherMonitor will not even show the CPU usage of the own app process, just memory values. Read the below [Retriving processes info since Android 7.0](#retrieving-processes-info-since-android-nougat-70-api-24-august-2016) for more info.

## Details

AnotherMonitor is ideal to find out the device CPU and memory resources state in real time.

It has 2 main options:
- It shows a graphic and several text labels wherein the values of the CPU and memory usage are updated every 0.5, 1, 2 or 4 seconds.
- It can record on a CSV file the read values for a later usage and process on a spreadsheet program.

The app can run in the background. Then, the second option is specially interesting since, in the background, AnotherMonitor consumes little resources and can monitor and record the CPU and memory values that other applications are using in the foreground.

From the buttons shown in the system bar the recording of values can be started and the app can be closed.

#### How CPU and memory usage are obtained

In order to get the CPU usage the app does NOT make use of the `Top` command from Linux but instead it parses `/proc/stat` and rest of process folders from the `procfs` file system and work out the calculations with the user and system time. This is implemented on [ServiceReader.class](https://github.com/AntonioRedondo/AnotherMonitor/blob/master/AnotherMonitor/src/main/java/org/anothermonitor/ServiceReader.java#L259). You can find more information about it on:
- [procfs - Wikipedia](https://en.wikipedia.org/wiki/Procfs)
- [Calculating CPU usage of a process in Linux - Stack Overflow](http://stackoverflow.com/questions/1420426/calculating-cpu-usage-of-a-process-in-linux)

#### About multi-core devices

The app does not support showing of information regarding a specific device's core in multi-core devices. The implementation of this functionality would require considerable time. So there is no schedule for this feature.

#### Retrieving processes info since Android Lollipop 5.1 (API 22, March 2015)

For devices running Android 5.1.1 it is not possible any more to retrieve the processes list with [ActivityManager.getRunningAppProcesses()](http://developer.android.com/reference/android/app/ActivityManager.html#getRunningAppProcesses%28%29). This change has not been documented anywhere on the official documentation nor an alternative way to retrieve the list has been provided. Thankfully the community at Stack Overflow came out with some [satisfying solution](http://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag). For devices with Android 5.1 AnotherMonitor now uses the unofficial [AndroidProcesses](https://github.com/jaredrummler/AndroidProcesses) library to retrieve the processes list. The drawback of this is that for some processes there is no way to retrieve the app name nor the icon and instead only the long package name is shown.

#### Retrieving processes info since Android Nougat 7.0 (API 24, August 2016)

In Android 7.0 Google once again has made undocumented changes and has significantly restricted access to the `proc` file system. This means that since this new API level it is not possible anymore to retrieve processes info unless you have root access. AnotherMonitor running on devices with Android 7.0 will only show total CPU usage, AnotherMonitor CPU usage and memory usage, but not CPU usage for other processes. [Star this issue](https://code.google.com/p/android/issues/detail?id=205565) to push Google to implement an alternative API.


#### Retrieving processes info since Android O 8.0 (API 26, Autunm 2017)

In Android 8.0 Google once again has made undocumented changes and has restricted even more the access to the `proc` file system. Until now it was possible to at least get process info of the process running the app and total CPU usage. This does not work any more. Furthermore [ActivityManager.getRunningServices(int)](https://developer.android.com/reference/android/app/ActivityManager.html#getRunningServices(int)) is being deprecated, alternative method which could be used to access processes info. AnotherMonitor running on devices with Android 8.0 will only show memory usage, but not total CPU usage or CPU usage for any process, includind the own app process.


## Resolving dependencies

AnotherMonitor only has one external dependency, [AndroidProcesses](https://github.com/jaredrummler/AndroidProcesses). It is used to retrieve the device processes list to populate the 'Processes' screen.

See the [Gradle](https://github.com/AntonioRedondo/AnotherMonitor/blob/master/AnotherMonitor/build.gradle) file for more details.

## Contribute!

If you have an awesome pull request no matter whether it's a big or small change, send it over! Your link to your GitHub account will be added below.

## License

AnotherMonitor makes use of the [GNU GPL v3.0](http://choosealicense.com/licenses/gpl-3.0/) license. Remember to make public your project source code when reusing AnotherMonitor code.
