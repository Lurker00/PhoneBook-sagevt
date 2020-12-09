ANDROID_DATA=/data/local/tmp/.sagevt/;
CLASSPATH=${ANDROID_DATA}sagevt.jar;
export ANDROID_DATA=/data/local/tmp/.sagevt/;
export CLASSPATH=${ANDROID_DATA}sagevt.jar;

exec app_process ${ANDROID_DATA} com.sagetech.sagevt.Main debug=true landscape=true dim=true
