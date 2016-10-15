import android,time
droid=android.Android()

droid.startLocating()
time.sleep(15)
loc = droid.readLocation().result
droid.stopLocating()

