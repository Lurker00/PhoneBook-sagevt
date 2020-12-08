@echo off
call gradlew.bat assembleRelease 2> errors.txt
call :chksize errors.txt
goto :eof

:chksize
if %~z1 == 0 (
	copy build\outputs\apk\release\sagevt-release-unsigned.apk sagevt.jar
) else (
	type errors.txt
)
