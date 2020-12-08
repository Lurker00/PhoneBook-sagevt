# What is it?
This repository is dedicated to a part of the firmware for [Anyware PhoneBook](https://github.com/Lurker00/PhoneBook/). If you have such a device, and
* use it with an Android smartphone or tablet,
* your Android device does not support Desktop mode, or Samsung DeX, or Huawei EMUI Desktop, or any similar technology,
* Anyware Android app has requested you to turn og USB Debugging,
* and you can operate your device using PhoneBook's touchscreen using multitouch,

most probably this firmware part is of your interest. In this particular case, when an Android device is connected to the PhoneBook, the firmware establish connection via adb, then injects a Java applet (`sagevt.jar`) and shell script (`script.sh`) into `/data/local/tmp/.sagevt` directory, and run the script that starts the applet. The Anyware app connects to the applet via TCP and sends touch events data to it. The applet receives the data and simulates touch events using Android API.

The trick is required, because a user space application has no permissions to use this API, but `adbd` process and its childs have.

The `sagevt.jar` file can be easily decompiled, at least to confirm that the applet performs only this particular taks and does not harm the user in either way.

This project is based on the decompiled code, and is aimed to improve its functionality. Currently, it can be run on a rooted device only (`su` and super-user permission are required). I hope that PhoneBook developers would either use this repository to improve PhoneBook functionality, or, atleast, would make corrections in the future updates to let end user to use custom versions (like this) on not rooted devices.

# What can it do?

This new version demonstrates the following possibilities:
* Change device screen resolution, e.g. to 1920x1080, to exactly match PhoneBook screen.
* Change device screen density. Smaller density on 15.6" screen is more useful, and Android can be turned into tablet UI, more suitable for big landscape screen.
* Force landscape orientation. All the apps, including those that usually request portrait orientation, work in landscape.
* Dim the screen brightness to the minimum, to save the battery.
* Activate input method editor (IME), more suitable for the physical keyboard. I use [External Keyboard Helper Pro](https://play.google.com/store/apps/details?id=com.apedroid.hwkeyboardhelper).
* Send two Back key events at the start and at the end of PhoneBook session, to make Anyware app to close. It starts automatically on device attach and detach events, which I find quite annoying.

When the device is disconnected from the PhoneBook, all the affected parameters are restored to their original values.
