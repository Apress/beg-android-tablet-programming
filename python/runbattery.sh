adb push battery2.py /sdcard/SL4A/scripts
adb shell am start -a com.googlecode.android_scripting.action.LAUNCH_FOREGROUND_SCRIPT -n com.googlecode.android_scripting/.activity.ScriptingLayerServiceLauncher -e com.googlecode.android_scripting.extra.SCRIPT_PATH /sdcard/SL4A/scripts/battery2.py

