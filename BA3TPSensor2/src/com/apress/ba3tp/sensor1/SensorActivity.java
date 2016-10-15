package com.apress.ba3tp.sensor1;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
  TextView mDisplay;
  TextView mAcclerometer;
  TextView mMagnetic;
  TextView mOrientation;
  float[] mGrav = new float[3];
  float[] mMag = new float[3];
  float[] mMatrix = new float[9];
  float[] mRemapped = new float[9];
  float[] mOrient = new float[3];
  int mRemap = 0; // 0=Normal, 1=camera, 2=compass 
  final String[] mMapNames = {"Normal","Camera","Compass"}; 
  SensorManager mSensorManager;
  List<Sensor> mListening = new ArrayList<Sensor>();
  
  private static enum MenuId {
    SHARE;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  } 

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mDisplay = (TextView) findViewById(R.id.eDisplay);
    mAcclerometer = (TextView) findViewById(R.id.tvAccelerometer);
    mMagnetic = (TextView) findViewById(R.id.tvMagnetic);
    mOrientation = (TextView) findViewById(R.id.tvOrientation);
    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
  }

  public void onListClick(View v) {
    List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ALL);
    StringBuilder b = new StringBuilder();
    for (Sensor sensor : list) {
      b.append(sensor.getName() +" (" +sensor.getType()+") "+sensor.getPower()+"mA\n");
    }
    mDisplay.setText(b);
  }
  
  // Sadly, TYPE_ORIENTATION is obsolete. This rather more complex design is more accurate anyway. 
  public void onOrientClick(View v) {
    doListen(Sensor.TYPE_ACCELEROMETER);
    doListen(Sensor.TYPE_MAGNETIC_FIELD);
  }
  
  public void onStopClick(View v) {
    clearListening();
  }
  
  public void onRemapClick(View v) {
    mRemap = (mRemap+1) % 3;
    mDisplay.setText("Remap="+mMapNames[mRemap]);
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
    mDisplay.setText("Accuracy changed.");
  }

  private void showEventInfo(SensorEvent event, TextView dest, String caption) {
    String s = caption+"\n"; 
    for(float v : event.values) {
      s+=v+"\n";
    }
    dest.setText(s);
  }
  
  private void showEvent(SensorEvent event, TextView dest) {
    showEventInfo(event,dest,"Event: "+event.sensor.getName());
  }
  
  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
      System.arraycopy(event.values, 0, mGrav, 0, 3); // Note copy, because this array will be reused.
      showEvent(event,mAcclerometer);
    } else if (event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
      System.arraycopy(event.values, 0, mMag, 0, 3); // Note copy, because this array will be reused.
      showEvent(event,mMagnetic);
    }
    if (SensorManager.getRotationMatrix(mMatrix, null, mGrav, mMag)) {
      if (mRemap==1 ) { // Camera - map X->X, Y->Z
        SensorManager.remapCoordinateSystem(mMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRemapped);
      }
      else if (mRemap==2) { // Compass, 90 degree rotation. Map X->Y, Y-> minus X
        SensorManager.remapCoordinateSystem(mMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRemapped);
      }
      else System.arraycopy(mMatrix, 0, mRemapped, 0, 9); // Just copy
      SensorManager.getOrientation(mRemapped, mOrient);
      String s = "Orientation:\n"+
        "azimuth="+Math.round(Math.toDegrees(mOrient[0]))+"\n"+
        "pitch="+Math.round(Math.toDegrees(mOrient[1]))+"\n"+
        "roll="+Math.round(Math.toDegrees(mOrient[2]));
      mOrientation.setText(s);  
    }
  }
  
  @Override
  public void onPause() {
    super.onPause();
    stopListening();
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

  // Menu Stuff  
  public void doShare() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_TEXT, mDisplay.getText());
    intent.putExtra(Intent.EXTRA_SUBJECT, "Sensor Dump");
    intent.setType("text/plain");
    startActivity(Intent.createChooser(intent, "Send Sensor info to:"));
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MenuId.SHARE.getId(), Menu.NONE, "Share").setIcon(
        android.R.drawable.ic_menu_share);
    return super.onCreateOptionsMenu(menu);
  }
 
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId==MenuId.SHARE.getId()) {
      doShare();
    }
    return super.onOptionsItemSelected(item);
  }
}