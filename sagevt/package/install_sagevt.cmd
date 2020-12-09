@echo off
adb shell su -c "killall sagevt.i"

adb shell chmod 777 /data/local/tmp/.sagevt
adb shell chmod 777 /data/local/tmp/.sagevt/*
adb shell su -c "rm -rf /data/local/tmp/.sagevt"

adb shell mkdir /data/local/tmp/.sagevt
adb push sagevt.jar /data/local/tmp/.sagevt
adb push script.sh  /data/local/tmp/.sagevt

adb shell chmod 555 /data/local/tmp/.sagevt/sagevt.jar
adb shell chmod 555 /data/local/tmp/.sagevt/script.sh

adb shell mkdir /data/local/tmp/.sagevt/oat
adb shell mkdir /data/local/tmp/.sagevt/dalvik-cache
adb shell chmod 555 /data/local/tmp/.sagevt
