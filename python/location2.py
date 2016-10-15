import android

droid = android.Android()

mylocation={} # Store last location shown.

def showmessage(msg):
  droid.dialogCreateAlert("Location",msg)
  droid.dialogSetPositiveButtonText("Geocode")
  droid.dialogShow()

def showlocation(data):
  global mylocation
  if data.has_key('gps'): # Use the more accurate gps if available
    location=data['gps']
  elif data.has_key('network'):
    location=data['network']
  else:
    showmessage('No location data')
    return
  mylocation=location
  showmessage("Location: %(provider)s\nLatitude=%(latitude)0.5f,Longitude=%(longitude)0.5f" \
      % location)

def getgeocode():
  global mylocation
  print mylocation
  showmessage('Getting geocode')
  result=droid.geocode(mylocation['latitude'],mylocation['longitude']).result
  s="Geocode"
  if len(result)<1:
    s=s+"\nUnknown"
  else:
    result=result[0]
    for k in result:
      s=s+"\n"+k+"="+str(result[k])
  showmessage(s)

def eventloop():
  while True:
    event=droid.eventWait().result
    name=event['name']
    data=event['data']
    if name=='location':
      showlocation(data)
    elif name=='dialog':
      if data.has_key('canceled'):
        break
      if data.has_key('which'):
        if data['which']=='positive':
          getgeocode()

droid.startLocating()
# It will take a little while to actually get a fresh location
# so start off using last known.
showlocation(droid.getLastKnownLocation().result)
eventloop()
droid.stopLocating()
droid.dialogDismiss()

