import android,sys,os
droid=android.Android()

def showdialog():
  volume=droid.getMediaVolume().result
  droid.dialogCreateSeekBar(volume,maxvolume,"Media Play","Volume")
  if droid.mediaIsPlaying("mp3").result:
    caption="Pause"
  else:
    caption="Play"
  droid.dialogSetPositiveButtonText(caption)
  droid.dialogSetNegativeButtonText("Rewind");
  droid.dialogShow()

def eventloop():
  while True:
    event=droid.eventWait().result
    print event
    data=event["data"]
    if event["name"]=="dialog":
      if data.has_key("canceled"):
        break
      which=data["which"]
      if which=="seekbar":
        droid.setMediaVolume(data["progress"])
      elif which=="positive":
        if droid.mediaIsPlaying("mp3").result:
          droid.mediaPlayPause("mp3")
        else:
          droid.mediaPlayStart("mp3")
        showdialog()
      elif which=="negative":
        droid.mediaPlaySeek(0,"mp3")
        showdialog()


def showerror(msg): # Display an error message
  droid.dialogCreateAlert("Error",msg)
  droid.dialogShow()
  droid.dialogGetResponse()

def findmp3(): # Search sdcard for an mp3 file.
  mylist=[]
  names=[]
  for root,dirs,files in os.walk("/sdcard"):
    for name in files:
      fname,fext = os.path.splitext(name)
      if fext.lower()==".mp3":
          mylist.append(os.path.join(root,name))
          names.append(fname)
  droid.dialogCreateAlert("MP3 File")
  droid.dialogSetItems(names)
  droid.dialogShow()
  result=droid.dialogGetResponse().result
  droid.eventClearBuffer() # Get rid of unwanted events
  print result
  if not result.has_key("canceled"):
    return mylist[result['item']]
  else:
    return None

maxvolume=droid.getMaxMediaVolume().result
mp3=findmp3()
if mp3==None:
  showerror("No media file chosen")
  sys.exit(0)  
if not droid.mediaPlay("file://"+mp3,"mp3",False).result:
  showerror("Can't open mp3 file.")
  sys.exit(0)
showdialog()  
eventloop()
droid.mediaPlayClose("mp3")
droid.dialogDismiss()

