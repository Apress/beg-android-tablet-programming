import android
droid = android.Android()
droid.batteryStartMonitoring()
health={1:"Unknown",2:"Good",3:"Overheat",4:"Dead",5:"Over voltage",6:"Failure"}
plug={-1:"unknown",0:"unplugged",1:"AC charger",2:"USB port"}
status={1:"unknown",2:"charging",3:"discharging",4:"not charging",5:"full"} 
droid.eventWaitFor("battery")
droid.eventClearBuffer() # eventWaitFor leaves event in queue.
print "Voltage: ",droid.batteryGetVoltage().result,"mV"
print "Present: ",droid.batteryCheckPresent().result
print "Health: ",health[droid.batteryGetHealth().result]
print "Level: ",droid.batteryGetLevel().result,"%"
print "Plug Type: ",plug[droid.batteryGetPlugType().result]
print "Status: ",status[droid.batteryGetStatus().result]
print "Technology: ",droid.batteryGetTechnology().result
print u"Temperature: %0.1f C" % (droid.batteryGetTemperature().result/10.0)
droid.batteryStopMonitoring()

