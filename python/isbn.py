import android
droid = android.Android()
code=droid.scanBarcode()
isbn= int(code.result['extras']['SCAN_RESULT'])
url="http://books.google.com?q=%d" % isbn
droid.startActivity('android.intent.action.VIEW', url)
