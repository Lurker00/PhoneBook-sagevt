@echo off
adb shell su -c "killall sagevt.i"

adb shell chmod 777 /data/local/tmp/.sagevt
adb shell chmod 777 /data/local/tmp/.sagevt/*
adb shell su -c "rm -rf /data/local/tmp/.sagevt"
