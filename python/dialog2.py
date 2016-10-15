import android
droid=android.Android()
droid.dialogCreateAlert("I like swords.","Do you like swords?")
droid.dialogSetPositiveButtonText("Yes")
droid.dialogSetNegativeButtonText("No")
droid.dialogShow()
response=droid.dialogGetResponse().result
droid.dialogDismiss()
if response.has_key("which"):
  result=response["which"]
  if result=="positive":
    print "Yay! I like swords too!"
  elif result=="negative":
    print "Oh. How sad."
elif response.has_key("canceled"): 
  print "You can't even make up your mind?"
else:
  print "Unknown response=",response

print "Done"

