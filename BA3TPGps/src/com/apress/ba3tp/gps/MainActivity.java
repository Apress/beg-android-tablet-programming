package com.apress.ba3tp.gps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.apress.ba3tp.gps.LocationHandler.LocationUpdater;
import com.apress.ba3tp.utils.MessageBox;

public class MainActivity extends Activity implements LocationUpdater,
    SensorEventListener, OnSharedPreferenceChangeListener {

  TextView mDisplay;
  TextView mDistance;
  ToggleButton mWake;
  LocationHandler mLocationHandler;
  Vibrator mVibrator;
  boolean mActive = false;
  boolean mCompass = false;
  boolean mCompassMap = false;
  Target mTarget;
  SharedPreferences mPreferences;
  SensorManager mSensorManager;
  LocationManager mLocationManager;
  TextView mOrientation;
  List<Sensor> mListening = new ArrayList<Sensor>();
  MenuItem mProximity;
  MessageBox mMessageBox;
  int mRemap = 0; // Compass
  float[] mGrav = new float[3];
  float[] mMag = new float[3];
  float[] mMatrix = new float[9];
  float[] mRemapped = new float[9];
  float[] mOrient = new float[3];
  long mLastBeep;
  boolean mBuzz = false;
  private long mWarnDistance;
  public static final String PROXIMITY_EVENT = "com.apress.ba3tp.gps.Proximity"; 

  private static enum MenuId {
    PREFERENCES,TARGETS,SAVE,PROXIMITY,SHOW,SHOWMAP,AVAILABLE;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

// Events  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mDisplay = (TextView) findViewById(R.id.eDisplay);
    mDistance = (TextView) findViewById(R.id.eDistance);
    mWake = (ToggleButton) findViewById(R.id.btnWake);
    mOrientation = (TextView) findViewById(R.id.eOrientation);
    mLocationHandler = new LocationHandler(this);
    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mPreferences.registerOnSharedPreferenceChangeListener(this);
    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    mMessageBox = new MessageBox(this,"GPS");
}

  @Override
  protected void onResume() {
    loadPreferences();
    setLocating(mActive);
    startCompass(mCompass);
    super.onResume();
  }

  @Override
  protected void onPause() {
    savePreferences();
    stopLocating();
    clearListening();
    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putBoolean("active", mActive);
    outState.putBoolean("compass", isCompass());
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
    loadPreferences();
    if (key.equals("distance") && IntentHolder.hasIntent()) { // If distance changed, reset proximity alert.
      startProximity();
    }
  }

  @Override
  protected void onDestroy() {
    mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    super.onDestroy();
  }

// Preferences
  private void loadPreferences() {
    if (mPreferences.contains("target")) {
      mTarget = new Target(mPreferences.getString("target", ""));
    }
    setCompass(mPreferences.getBoolean("compass", false));
    setActive(mPreferences.getBoolean("active", false));
    mBuzz=mPreferences.getBoolean("buzz", false);
    mWarnDistance = Long.parseLong(mPreferences.getString("distance","500"),10);
    mCompassMap = mPreferences.getBoolean("remap", false);
    mRemap = mCompassMap ? 2 : 0; // Remap compass if required.
    boolean wakeup = mPreferences.getBoolean("wakeup",false); 
    doService(wakeup);
    mWake.setChecked(wakeup);
  }

  private void savePreferences() {
    SharedPreferences.Editor e = mPreferences.edit();
    if (mTarget != null) {
      e.putString("target", mTarget.getAsString());
    }
    e.putBoolean("compass", isCompass());
    e.putBoolean("active", isActive());
    e.commit();
  }

  // Menus

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences")
    .setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(Menu.NONE, MenuId.TARGETS.getId(), Menu.NONE, "Targets")
    .setIcon(android.R.drawable.ic_menu_compass);
    menu.add(Menu.NONE, MenuId.SAVE.getId(), Menu.NONE, "Save")
    .setIcon(android.R.drawable.ic_menu_save);
    mProximity=menu.add(Menu.NONE, MenuId.PROXIMITY.getId(), Menu.NONE, "Proximity Alert");
    menu.add(Menu.NONE, MenuId.SHOW.getId(), Menu.NONE, "Show Target")
    .setIcon(android.R.drawable.ic_menu_view);
    menu.add(Menu.NONE, MenuId.SHOWMAP.getId(), Menu.NONE, "Show Map")
    .setIcon(android.R.drawable.ic_menu_mapmode);
    menu.add(Menu.NONE, MenuId.AVAILABLE.getId(), Menu.NONE, "Available");
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, Preferences.class));
    } else if (id == MenuId.TARGETS.getId()) {
      Intent intent = new Intent(this,ManageList.class);
      startActivity(intent);
    } else if (id == MenuId.SAVE.getId()) {
      if (mTarget!=null) {
        ManageList.addList(mTarget, mPreferences);
        toast("Target saved to list");
      }
    } else if (id == MenuId.PROXIMITY.getId()) {
      startProximity();
    } else if (id == MenuId.SHOW.getId()) {
      if (mTarget!=null) {
        Uri geo = Uri.parse("geo:"+mTarget.latitude+","+mTarget.longitude+"?z=18");
        Intent intent = new Intent(Intent.ACTION_VIEW,geo);
        startActivity(intent);
      }
    } else if (id == MenuId.SHOWMAP.getId()) {
      if (mTarget!=null) {
        try {
          Uri geo = Uri.parse("http://maps.google.com.au/maps?q=loc:"+mTarget.latitude+","+mTarget.longitude+"&z=20");
          Intent intent = new Intent(Intent.ACTION_VIEW,geo);
          startActivity(intent); 
        } catch (Exception e) {
          toast(e);
        }
      }
    } else if (id==MenuId.AVAILABLE.getId()) {
      StringBuilder b = new StringBuilder("Available Providers");
      for (String name : mLocationHandler.getProviders()) {
        LocationProvider provider=mLocationManager.getProvider(name);
        b.append("\n"+provider.getName()+" "+(mLocationManager.isProviderEnabled(name)? "enabled" : "disabled"));
        b.append("\n  Power "+LocationHandler.getPowerName(provider.getPowerRequirement())
            +" Acc: "+LocationHandler.getAccuracyName(provider.getAccuracy()));
        
        String status = mLocationHandler.readStatus().get(name);
        if (status!=null) b.append("\n  Status: "+status);
      }
      mMessageBox.showMessage(b);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (IntentHolder.hasIntent()) mProximity.setIcon(android.R.drawable.star_on);
    else mProximity.setIcon(android.R.drawable.star_off);
    return super.onPrepareOptionsMenu(menu);
  }

// Button methods
  public void onButtonClick(View v) {
    setActive(true);
  }

  public void onSetTargetClick(View v) {
    Map<String, Location> m = mLocationHandler.readLocation();
    if (m == null) {
      toast("No Location information yet.");
    } else if (m.containsKey("gps")) {
      mTarget = new Target(m.get("gps"));
      toast("fine location saved");
    } else if (m.containsKey("network")) {
      mTarget = new Target(m.get("network"));
      toast("coarse location saved.");
    }
  }

  public void onCompassClick(View v) {
    ToggleButton b = (ToggleButton) v;
    setCompass(b.isChecked());
    startCompass(isCompass());
  }

  public void onWakeClick(View v) {
    SharedPreferences.Editor e = mPreferences.edit();
    e.putBoolean("wakeup", ((ToggleButton) v).isChecked());
    e.commit();
  }

  public void onGpsClick(View v) {
    ToggleButton b = (ToggleButton) v;
    setLocating(b.isChecked());
  }


// Application Functions  
  void startLocating() {
    mLocationHandler.startLocating(0, 0);
    mLocationHandler.setUpdater(this);
  }

  void stopLocating() {
    mLocationHandler.stopLocating();
    mLocationHandler.setUpdater(null);
  }

  boolean isActive() {
    return mActive;
  }

  void setActive(boolean active) {
    mActive = active;
    ((ToggleButton) findViewById(R.id.toggleButton2)).setChecked(active);
  }

  public void setLocating(boolean onOff) {
    setActive(onOff);
    if (onOff) {
      startLocating();
      display("Listening for locations.");
      for (String s : mLocationHandler.getProviders()) {
        addln(s);
      }
    } else {
      mLocationHandler.stopLocating();
      addln("Stopped");
    }
  }

  void doService(boolean onOff) {
    Intent background = new Intent(this, NotifyService.class);
    if (onOff) {
      startService(background);
    } else {
      stopService(background);
    }
  }
  
  void startProximity() {
    if (mTarget!=null) {
      boolean onOff = !IntentHolder.hasIntent();
      clearProximity();
      if (onOff) {
        addProximityAlert(mTarget.latitude,mTarget.longitude);
        toast("Proximity alert set.");
      } else {
        toast("Proximity alert cleared.");
      }
    }
  }

  public void clearProximity() {
    IntentHolder ih = IntentHolder.getInstance();
    if (IntentHolder.hasIntent()) {
      mLocationManager.removeProximityAlert(ih.getIntent());
      ih.setIntent(null);
    }
  }
  private void addProximityAlert(double latitude, double longitude) {

    Intent intent = new Intent(PROXIMITY_EVENT);
    PendingIntent proximityIntent = PendingIntent.getBroadcast(this, 0, intent,
        0);
    IntentHolder.getInstance().setIntent(proximityIntent);
    mLocationManager.addProximityAlert(latitude, // the latitude of the central point of the alert region
        longitude, // the longitude of the central point of the alert region
        mWarnDistance, // the radius of the central point of the alert region, in meters
        -1, // time for this proximity alert, in milliseconds, or -1 to indicate no expiration
        proximityIntent // will be used to generate an Intent to fire when entry to or exit from the alert region is detected
        );

    IntentFilter filter = new IntentFilter(PROXIMITY_EVENT);
    registerReceiver(new ProximityIntentReceiver(), filter);
  }

  // Graphical Display
  public void drawCompass() {
    ImageView image = (ImageView) findViewById(R.id.imageView1);
    Bitmap b = Bitmap.createBitmap(image.getWidth(), image.getHeight(),
        Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    c.drawARGB(255, 255, 255, 255); // White fill.
    Paint p = new Paint();
    p.setARGB(255, 255, 0, 0);
    int cx = b.getWidth() / 2;
    int cy = b.getHeight() / 2;
    c.drawCircle(cx, cy, cx, p);
    p.setARGB(255, 0, 0, 0); // Black;
    c.drawLine(cx, 0, cx, c.getHeight(), p);
    c.drawLine(0, cy, c.getWidth(), cy, p);
    p.setTextSize(c.getHeight() / 6);
    p.setTypeface(Typeface.DEFAULT_BOLD);
    p.setTextAlign(Align.CENTER);
    FontMetrics fm = new FontMetrics();
    Rect bounds = new Rect();
    p.getTextBounds("W", 0, 1, bounds);
    int lh = bounds.height();
    int north = (int) Math.toDegrees(mOrient[0]);
    c.rotate(-north, cx, cy);
    c.drawText("N", cx, lh, p);
    c.drawText("S", cx, c.getHeight() - fm.descent, p);
    c.drawText("W", p.measureText("W"), cy + (lh / 2), p);
    c.drawText("E", c.getWidth() - p.measureText("E"), cy + (lh / 2), p);
    p.setARGB(192, 0, 255, 0);
    Map<String, Location> locations = mLocationHandler.readLocation();
    if (locations != null && locations.containsKey("gps") && mTarget != null) {
      Location loc = locations.get("gps");
      int bearing = (int) bearing(new Target(loc), mTarget);
      c.rotate(bearing, cx, cy);
      c.drawCircle(cx, 8, 8, p);
    }
    image.setImageBitmap(b);
    updateInfo();
  }

  void updateInfo() {
    StringBuilder b = new StringBuilder();
    int north = (int) Math.toDegrees(mOrient[0]);
    if (north < 0)
      north += 360;
    b.append("Direction: " + north);
    Map<String, Location> maps = mLocationHandler.readLocation();
    if (maps != null && maps.containsKey("gps") && mTarget != null) {
      Location location = maps.get("gps");
      Target here = new Target(location);
      long d = (long) (distance(here, mTarget) * 1000);
      String distance = d + "m";
      b.append("\nDistance: " + d + "m");
      int angle = (int) bearing(here, mTarget);
      if (angle < 0)
        angle += 360;
      b.append("\nBearing: " + angle);
      b.append("\nSpeed: " + location.getSpeed() * 3.6 + "k");
      b.append("\nAccuracy: " + location.getAccuracy() + "m");
      Date now = new Date();
      long elapsed = (now.getTime() - location.getTime()) / 1000;
      b.append("\nLast update: " + elapsed);
      if (d <= location.getAccuracy()) {
        distance = "HERE! (" + d + "m)";
        if (mBuzz) {
          if ((now.getTime() - mLastBeep) >= 10000) { // Buzz every 10 seconds
            mLastBeep = now.getTime();
            mVibrator.vibrate(500);
          }
        }
      }
      mDistance.setText(distance);
    }
    mOrientation.setText(b.toString());
  }

  public boolean isCompass() {
    return mCompass;
  }

  public void setCompass(boolean onOff) {
    ((ToggleButton) findViewById(R.id.toggleButton1)).setChecked(onOff);
    mCompass = onOff;
  }

  public void startCompass(boolean onOff) {
    if (onOff) {
      doListen(Sensor.TYPE_ACCELEROMETER);
      doListen(Sensor.TYPE_MAGNETIC_FIELD);
    } else {
      clearListening();
    }
  }

  // Utility methods
  void toast(Object o) {
    Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
  }

  // Show a message on the screen
  public void display(Object msg) {
    mDisplay.setText(msg.toString());
  }

  public void clear() {
    mDisplay.setText("");
  }

  // Add a line to the existing message
  public void addln(Object msg) {
    String s = (String) mDisplay.getText();
    if (s.equals(""))
      s = msg.toString();
    else
      s += "\n" + msg.toString();
    mDisplay.setText(s);
  }

  
  @Override
  public void locationUpdate(LocationHandler handler, Location location) {
    // addln("Location: "+location);
    Map<String, Location> m = handler.readLocation();
    display("Update...");
    for (Location x : m.values()) {
      addln(x.getProvider() + " " + x.getTime() + "\n" + x.getLatitude() + ","
          + x.getLongitude());
    }
    if (mTarget != null) {
      Target here = new Target(location);
      long d = (long) (distance(here, mTarget) * 1000);
      addln("Distance to target: " + d + "m");
      addln("Bearing: " + bearing(here, mTarget));
      addln("Speed: " + location.getSpeed() * 3.6 + "kmh");
      addln("Accuracy: " + location.getAccuracy());
    }
    drawCompass();
  }

  public static double distance(Target from, Target to) {
    // Implmentation of the 'haversine' formula
    // Returns distance in KM.
    long R = 6371; // km
    double dLat = Math.toRadians(to.latitude - from.latitude);
    double dLon = Math.toRadians(to.longitude - from.longitude);
    double lat1 = Math.toRadians(from.latitude);
    double lat2 = Math.toRadians(to.latitude);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
        * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = R * c;
    return d;
  }

  public static double bearing(Target from, Target to) {
    // Great circle bearing.
    // double dLat = Math.toRadians(to.latitude-from.latitude);
    double dLon = Math.toRadians(to.longitude - from.longitude);
    double lat1 = Math.toRadians(from.latitude);
    double lat2 = Math.toRadians(to.latitude);
    double y = Math.sin(dLon) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
        * Math.cos(lat2) * Math.cos(dLon);
    double brng = Math.toDegrees(Math.atan2(y, x));
    return brng;
  }

  public void doListen(int sensorType) {
    List<Sensor> sensors = mSensorManager.getSensorList(sensorType);
    if (sensors.size() == 0) {
      mDisplay.setText("No Sensor available.");
      return;
    }
    Sensor sensor = sensors.get(0); // Lazily just grab first one.
    if (!mListening.contains(sensor)) {
      if (mSensorManager.registerListener(this, sensor,
          SensorManager.SENSOR_DELAY_NORMAL)) {
        mListening.add(sensor);
        mDisplay.setText("Listening to " + sensor.getName());
      } else {
        mDisplay.setText("Listening failed.");
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // mDisplay.setText("Accuracy changed.");
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      System.arraycopy(event.values, 0, mGrav, 0, 3); // Note copy, because this
                                                      // array will be reused.
    } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      System.arraycopy(event.values, 0, mMag, 0, 3); // Note copy, because this
                                                     // array will be reused.
    }
    if (SensorManager.getRotationMatrix(mMatrix, null, mGrav, mMag)) {
      if (mRemap == 1) { // Camera - map X->X, Y->Z
        SensorManager.remapCoordinateSystem(mMatrix, SensorManager.AXIS_X,
            SensorManager.AXIS_Z, mRemapped);
      } else if (mRemap == 2) { // Compass, 90 degree rotation. Map X-> minus Y, Y-> X
        SensorManager.remapCoordinateSystem(mMatrix, SensorManager.AXIS_MINUS_Y,
            SensorManager.AXIS_X, mRemapped);
      } else
        System.arraycopy(mMatrix, 0, mRemapped, 0, 9); // Just copy
      SensorManager.getOrientation(mRemapped, mOrient);
      String s = "Orientation:\n" + "Direction="
          + Math.round(Math.toDegrees(mOrient[0]));
      /*
       * +"\n"+ "pitch="+Math.round(Math.toDegrees(mOrient[1]))+"\n"+
       * "roll="+Math.round(Math.toDegrees(mOrient[2]));
       */
      mOrientation.setText(s);
      drawCompass();
    }
  }

  public void stopListening() {
    for (Sensor sensor : mListening) {
      mSensorManager.unregisterListener(this, sensor);
    }
  }

  public void clearListening() {
    stopListening();
    mListening.clear();
    mDisplay.setText("Listening stopped.");
  }

}
