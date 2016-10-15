package com.apress.ba3tp.gps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class NotifyService extends Service implements OnSharedPreferenceChangeListener,LocationHandler.LocationUpdater {
  public static final int NOTIFY_ID = 1;
  NotificationManager mNotificationManager;
  private Notification mNotification;
  private SharedPreferences mPreferences;
  private LocationHandler mLocationHandler;
  private Target mTarget;
  private long mWarnDistance;
  private long mLastBeep;
  
  @Override
  public void onCreate() {
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mPreferences.registerOnSharedPreferenceChangeListener(this);
    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mNotification = createNotify();
    loadPreferences();
    mLocationHandler = new LocationHandler(this);
    mLocationHandler.setUpdater(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    doNotify("Listening for Wakeup",false);
    mLocationHandler.startLocating(30000, 0);
    startForeground(NOTIFY_ID, mNotification);
    // If we get killed, after returning from here, restart
    return START_STICKY;
  }

  public static Notification createNotify() {
    return new Notification(android.R.drawable.ic_menu_rotate,"Wake Me Up",System.currentTimeMillis());
  }
  
  public static void showNotify(Context context, Notification notify, String msg, boolean sound) {
    Intent notificationIntent = new Intent(context, MainActivity.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    notify.defaults &= (~Notification.DEFAULT_SOUND);
    if (sound) {
      String ringtone = PreferenceManager.getDefaultSharedPreferences(context).getString("ringtone", "");
      if (ringtone.equals("")) {
        notify.defaults |= Notification.DEFAULT_SOUND;
      } else {
        notify.sound=Uri.parse(ringtone);
      }
    }
    notify.tickerText=msg;
    notify.setLatestEventInfo(context, "Dude", msg, contentIntent);
    NotificationManager nm =(NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    nm.notify(NOTIFY_ID, notify);
  }

  public void doNotify(String msg, boolean sound) {
    showNotify(this,mNotification,msg,sound);
  }
  
  @Override
  public IBinder onBind(Intent intent) {
    // We don't provide binding, so return null
    return null;
  }

  @Override
  public void onDestroy() {
    mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    mLocationHandler.setUpdater(null);
    mLocationHandler.stopLocating();
    if (mNotification!=null) {
      String ns = Context.NOTIFICATION_SERVICE;
      NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
      mNotificationManager.cancel(NOTIFY_ID);
      mNotification=null;
    }
    Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
  }

  public void loadPreferences() {
    if (mPreferences.contains("target")) {
      mTarget = new Target(mPreferences.getString("target", ""));
    }
    mWarnDistance = Long.parseLong(mPreferences.getString("distance","500"),10);
  }

  @Override
  public void locationUpdate(LocationHandler handler, Location location) {
    if (location.getProvider().equals("gps") && mTarget!=null) {
      Target here = new Target(location);
      long distance=(long) (MainActivity.distance(here, mTarget)*1000);
      if (distance<=mWarnDistance) {
        long now = System.currentTimeMillis();
        if (now-mLastBeep>10000) {
          doNotify("Distance "+distance,true);
          mLastBeep=now;
        }
      }
      
    }
  }
}