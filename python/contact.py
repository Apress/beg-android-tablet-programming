import android
droid = android.Android()
contact=droid.pickContact().result
if contact==None:
  print "Nothing selected."
else:
  print contact["data"]
  details=droid.queryContent(contact["data"]).result
  for row in details:
    for k in row:
      print k,"=",row[k]

