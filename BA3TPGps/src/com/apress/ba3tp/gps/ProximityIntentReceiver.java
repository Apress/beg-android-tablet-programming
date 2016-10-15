package com.apress.ba3tp.gps;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {
  
  @Override
  public void onReceive(Context context, Intent intent) {
       
      String key = LocationManager.KEY_PROXIMITY_ENTERING;

      Boolean entering = intent.getBooleanExtra(key, false);
      IntentHolder.getInstance().setIntent(null);
      if (entering) {
          Log.d(getClass().getSimpleName(), "entering");
      }
      else {
          Log.d(getClass().getSimpleName(), "exiting");
      }
      Notification n = NotifyService.createNotify();
      n.flags |= Notification.FLAG_AUTO_CANCEL;
      NotifyService.showNotify(context, n, "Promximity Alert", true);
      // Remove altert.
      // Not sure this is completely necessary, but still...
      if (IntentHolder.hasIntent()) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        lm.removeProximityAlert(IntentHolder.getInstance().getIntent());
        IntentHolder.getInstance().setIntent(null);
        
      }
  }
   
}
