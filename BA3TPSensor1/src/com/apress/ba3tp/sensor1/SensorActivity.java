package com.apress.ba3tp.sensor1;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
  TextView mDisplay;
  SensorManager mSensorManager;
  Sensor mListening = null;
  
  private static enum MenuId {
    SHARE,MAIL;
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
  
  public void onLightClick(View v) {
    doListen(Sensor.TYPE_LIGHT);
  }
  
  public void onMagClick(View v) {
    doListen(Sensor.TYPE_MAGNETIC_FIELD);
  }

  public void onOrientClick(View v) {
    doListen(Sensor.TYPE_ORIENTATION);
  }
  
  public void onAccClick(View v) {
    doListen(Sensor.TYPE_ACCELEROMETER);
  }
  
  public void onProxClick(View v) {
    doListen(Sensor.TYPE_PROXIMITY);
  }
  
  public void doListen(int sensorType){
    if (mListening!=null) {
      stopListening();
      mDisplay.setText("stopped");
      return;
    }
    List<Sensor> sensors = mSensorManager.getSensorList(sensorType);
    if (sensors.size()==0) {
      mDisplay.setText("No appropriate Sensor available.");
      return;
    }
    stopListening(); // Make sure we've only go one listening at a time.
    mListening = sensors.get(0); // Lazily just grab first one.
    if (mSensorManager.registerListener(this, mListening, SensorManager.SENSOR_DELAY_NORMAL)) {
      mDisplay.setText("Listening to "+mListening.getName());
    } else {
      mDisplay.setText("Listening failed.");
    }  
  }

  public void stopListening() {
    if (mListening!=null) {
      mSensorManager.unregisterListener(this, mListening);
      mListening=null;
    }  
  }


  // Listener events
  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    mDisplay.setText("Accuracy changed.");
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    String s = "Event: "+event.sensor.getName()+"\n";
    for(float v : event.values) {
      s+=v+"\n";
    }
    mDisplay.setText(s);
  }
  
  @Override
  public void onPause() {
    super.onPause();
    stopListening();
  }
  
  // Menu Stuff  
  public void doShare() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_TEXT, mDisplay.getText());
    intent.putExtra(Intent.EXTRA_SUBJECT, "Sensor Dump");
    intent.setType("text/plain");
    startActivity(Intent.createChooser(intent, "Send Sensor info to:"));
  }

  public void doMail() {
    Intent intent = new Intent(Intent.ACTION_SENDTO);
    intent.putExtra(Intent.EXTRA_TEXT, mDisplay.getText());
    intent.putExtra(Intent.EXTRA_SUBJECT, "Sensor Email");
    intent.setData(Uri.parse("mailto:sales@apress.com"));
    try {
      startActivity(intent);
    } catch (Exception e) {
      mDisplay.setText(e.toString());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MenuId.SHARE.getId(), Menu.NONE, "Share").setIcon(
        android.R.drawable.ic_menu_share);
    menu.add(Menu.NONE, MenuId.MAIL.getId(), Menu.NONE, "Mail").setIcon(
        android.R.drawable.ic_menu_send);
    return super.onCreateOptionsMenu(menu);
  }
 
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId==MenuId.SHARE.getId()) {
      doShare();
    } else if (itemId==MenuId.MAIL.getId()) {
      doMail();
    }
    
    return super.onOptionsItemSelected(item);
  }
}