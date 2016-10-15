import android
droid = android.Android()
myconst = droid.getConstants("android.content.Intent").result
action = myconst["ACTION_VIEW"]
uri = "content://android.provider.Contacts.People.CONTENT_URI"
itype = "vnd.android.cursor.dir/calls"
intent = droid.makeIntent(action,uri,itype).result
print intent
droid.startActivityIntent(intent)

