import android

droid = android.Android()
version=droid.getConstants("android.os.Build$VERSION").result
print version["SDK_INT"]

