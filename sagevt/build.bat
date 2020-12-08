@echo off
call gradlew.bat assembleRelease 2> errors.txt
call :chksize errors.txt
goto :eof

:chksize
if %~z1 == 0 (
	mkdir release 2> NUL
	copy build\outputs\apk\release\sagevt-release-unsigned.apk release\sagevt.jar
) else (
	type errors.txt
)
