**WARNING: Please read the whole description with attention, to avoid possible problems!**<br />
**DISCLAIMER: This software extensively uses undocumented API. I've tested it under Android 6-10 emulators, but real world devices may have different behavior. No warranty at all: use it at your own risk and responsibility!**

# What is it?
This repository is dedicated to a part of the firmware for [Anyware PhoneBook](https://github.com/Lurker00/PhoneBook/). If you have such a device, and
* use it with an Android smartphone or tablet,
* your Android device does not support Desktop mode, or Samsung DeX, or Huawei EMUI Desktop, or any similar technology,
* Anyware Android app has requested you to turn og USB Debugging,
* and you can operate your device using PhoneBook's touchscreen using multitouch,

most probably this firmware part is of your interest. In this particular case, when an Android device is connected to the PhoneBook, the firmware establishes a connection via adb, then injects a Java applet (`sagevt.jar`) and shell script (`script.sh`) into `/data/local/tmp/.sagevt` directory, and runs the script that starts the applet. The Anyware app connects to the applet via TCP and sends touch events data to it. The applet receives the data and simulates touch events using Android API.

This trick is required because a user space application has no permissions to use touch simulation API, but `adbd` process and its childs have.

The `sagevt.jar` file can be easily decompiled, at least to confirm that the applet performs only this particular taks and does not harm the user in either way.

This project is based on the decompiled code, and is aimed to improve its functionality. Currently, it can be run on a rooted device only (`su` and super-user permission are required). I hope that PhoneBook developers would either use this repository to improve PhoneBook functionality, or, atleast, would make corrections in the future updates to let end user to use custom versions (like this) on not rooted devices.

The new functionality is based on my port of [scrcpy](https://github.com/Lurker00/scrcpy), with additions created especially for this project.

# What can it do?
This new version demonstrates the following possibilities:
* Change device screen resolution, e.g. to 1920x1080, to exactly match PhoneBook screen.
* Change device screen density. Smaller density on 15.6" screen is more useful, and Android can be turned into tablet UI, more suitable for big landscape screen.
* Force landscape orientation. All the apps, including those that usually request portrait orientation, work in landscape.
* Dim the screen brightness to the minimum, to save the battery.
* Activate input method editor (IME), more suitable for the physical keyboard. I use [External Keyboard Helper Pro](https://play.google.com/store/apps/details?id=com.apedroid.hwkeyboardhelper).
* Send two <kbd>Back</kbd> key events at the start and at the end of PhoneBook session, to make Anyware app to close. It starts automatically on device attach and detach events, which I find quite annoying.

When the device is disconnected from the PhoneBook, all the affected parameters are restored to their original values.

# The known problems and workarounds
## Anyware app does not handle screen resolution change
This renders the #1 feature useless: the app continues re-calculating touch coordinates to the resolution at which the device was attached to the PhoneBook. But you can try to see how might it be! This can be fixed by Anyware with easy.

You can use Android shell `wm` command to change the screen resolution before attaching the device to the PhoneBook, and `adb shell wm size reset` after that.

## Android kills the applet process on device detach
When an Android device disconnects from PhoneBook, the device's USB port state changes leading to adb restart. Android's `init` process kills all the service child processes on `stop service` command (`issued for `adbd` here). This makes impossible graceful applet shutdown with restoring the device to the original state. The current workaround is exactly to use `su`: the `init` has no permissions to kill the applet launched under `su`.

## PhoneBook firmware injects the files on each device attach
This is solved by the installation script I wrote, that protects the files by fiesystem permissions. A good side effect is that applet's Dalvik cache is kept between sessions, making the 2nd and further runs slightly faster.

## Android restriction for screen resolution
Android does not allow to set the screen size more than twice than the physical size. I.e. if your screen has height (in portrait) less than 960 pixels, you can't set it to 1920 or more. This should not be a problem for most devices on the market, but my phone has 240x432 screen and Android patched against this restriction.

## Your Android build may be not supported
My version of the applet extensively uses undocumented API. I've tested it under Android 6-10 emulators, but real world devices may have different behavior.

# How to use?
## First of all:
* You have to be familiar with command line, Windows or Linux.
* You need to have installed and working `adb`.
* If you are Windows user, you need a text editor that aware of Unix/Linux text file line ends. It seems that Windows 10 Notepad works correctly. [Notepad++](https://notepad-plus-plus.org/) is recommended.

I provide installation scripts as Windows `cmd`-files only. They contain nothing but `adb` calls, so I hope any Linux user can convert them to shell scripts.

## Make your device ready
To prepare your Android device, connect it to PC and, from console, type commands `adb shell`, then `su`. Check in the console that `su` is not unknown command. Check the device screen to grant su permission to the shell, without asking next time. Type `ls /data/local/tmp` to be sure your `su` is actually working. Type `exit` twice to quit su and shell.

Type `adb shell wm size` to check your current screen size *and* the order of the screen width and height. Usually it is `widthxheight`, presuming portrait orientation, but some devices may have physically landscape orientation.

Type `adb shell wm density` to chech the current pixel density, to start from.

## The package
Please download the [most recent release](https://github.com/Lurker00/PhoneBook-sagevt/releases/latest). The release zip-archive contains the following files:
* `sagevt.jar` - the applet to be installed on your Android device.
* `script.sh` - the launch script to be installed on your Android device. It contains the applet's command line to edit for optional parameters.
* `install_sagevt.cmd` - Windows installation script.
* `run_sagevt.cmd` - Windows script to install and then run the applet. Useful to tune the parameters.
* `uninstall_sagevt.cmd` - Windows script to uninstall the package.

Unzip the files into an empty directory.

## The applet parameters
Each parameter has a form of `name=value`, with no spaces inside. Read below for supported parameters with example values:
* `debug=true` - feeds applet debug output to the console. Default: false.
* `size=1080x1920` - sets screen resolution. Default: don't change.
* `density=220` - sets the pixel density (220 is recommended for 1080x1920). Default: don't change.
* `dim=true` - dims the screen backlight. Default: false.
* `landscape=true` - forces landscape orientation. Default: false.
* `sendback=true` - sends two <kbd>Back</kbd> key press events on device attach and detach, to close Anyware app. Default: false.
* `ime=com.apedroid.hwkeyboardhelper/.IME` - use this IME during PhoneBook session. Default: don't change. The example is for [External Keyboard Helper Pro](https://play.google.com/store/apps/details?id=com.apedroid.hwkeyboardhelper).
* `su=true` - uses su to fork the worker process. Default: true (as the [workaround](#anyware-app-kills-the-applet-process-on-device-detach)).

One more option that is only possible while the applet runs with su rights:
* `disableHWOverlays=true` = turn Developer option "Disable HW Overlays" on. Default: false. This option may be required if the selected screen resolution is higher than physical device screen resolution.

Example of my `script.sh`:
```
ANDROID_DATA=/data/local/tmp/.sagevt/;
CLASSPATH=${ANDROID_DATA}sagevt.jar;
export ANDROID_DATA=/data/local/tmp/.sagevt/;
export CLASSPATH=${ANDROID_DATA}sagevt.jar;
    
exec app_process ${ANDROID_DATA} com.sagetech.sagevt.Main debug=true density=220 landscape=true dim=true disableHWOverlays=true sendback=true ime=com.apedroid.hwkeyboardhelper/.IME
```
## The use
Connect your Android device to the PC, launch console, go to the directory with unzipped package files and run `run_sagevt.cmd`. The parameters from the package are: `debug=true landscape=true dim=true`. You should see the output like this:
```
killall: sagevt.i: No such process
sagevt.jar: 1 file pushed. 2.6 MB/s (20698 bytes in 0.008s)
script.sh: 1 file pushed. 0.0 MB/s (260 bytes in 0.006s)
sagevt is forking: 8858
sagevt forked
started: 8888<-8858
Start listening
DeviceControl started
ScreenRotation started
baseBrightness: 150
Orientation: 0
```
The device should change to reflect selected parameters. Press <kbd>Ctrl</kbd><kbd>C</kbd> or disconnect the cable to stop the applet. The device should restore its state.

Now connect your Android device to the PhoneBook to see the result in real life.

## Uninstall
Connect the device to PC and run `uninstall_sagevt.cmd`. Next time you attach the device to the PhoneBook, the standard applet will be injected and used.

# Troubleshooting
If something went wrong, the following commands should bring your device into a useful state:
```
adb shell su -c "killall sagevt.i"
adb shell wm size reset 
adb shell wm density reset
adb shell settings put system screen_brightness 150
```
The first command ensures that the applet is not running.

You may restart your device after that.

# Comments? Questions?
Welcome to this forum and thread: https://phonebook-mania.boards.net/thread/66/more-phonebook-android-device. Please don't try to contact me by other means regrding this projetc while this link remains here.
