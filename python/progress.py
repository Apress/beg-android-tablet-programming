import android,time
droid=android.Android()
droid.dialogCreateHorizontalProgress("My Progress","Snoozing",10)
droid.dialogShow()
for i in range(10):
  droid.dialogSetCurrentProgress(i)
  time.sleep(1)
droid.dialogDismiss()

