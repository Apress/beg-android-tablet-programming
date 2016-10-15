# Test of Seekbar events.
import android
droid=android.Android()
droid.dialogCreateSeekBar(50,100,"I like swords.","How much you like swords?")
droid.dialogSetPositiveButtonText("Yes")
droid.dialogSetNegativeButtonText("No")
droid.dialogShow()
looping=True
while looping: # Wait for events for up to 10 seconds .from the menu.
  response=droid.eventWait(10000).result
  if response==None: # No events to process. exit.
    break
  if response["name"]=="dialog":
    looping=False # Fall out of loop unless told otherwise.
    data=response["data"]
    if data.has_key("which"):
      which=data["which"]
      if which=="seekbar":
        print "Progress=",data["progress"]," User input=",data["fromuser"]
        looping=True  # Keep Looping

# Have fallen out of loop. Close the dialog 
droid.dialogDismiss()
if response==None:
  print "Timed out."
else:
  rdialog=response["data"] # dialog response is stored in data.
  if  rdialog.has_key("which"):
    result=rdialog["which"]
    if result=="positive":
      print "Yay! I like swords too!"
    elif result=="negative":
      print "Oh. How sad."
  elif rdialog.has_key("canceled"): 
    print "You can't even make up your mind?"
  print "You like swords this much: ",rdialog["progress"]  

print "Done"

