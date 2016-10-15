package com.apress.ba3tp.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ToggleButton;

public class TimersActivity extends Activity {
  private ProgressBar mProgress;
  private ProgressBar mHorizontal;
  private RadioButton mRadio;
  
  private ToggleButton mToggle;
  private Handler mHandler;
  private ToggleTick mToggleTick = new ToggleTick();
  private ClockTick mClockTick = new ClockTick();
  private SimpleDateFormat mDateFmt = new SimpleDateFormat("hh:mm:ss");
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timers);
    mProgress = (ProgressBar) findViewById(R.id.progressBar1);
    mHorizontal = (ProgressBar) findViewById(R.id.progressBar2);
    mRadio = (RadioButton) findViewById(R.id.radioButton1);
    mToggle = (ToggleButton) findViewById(R.id.toggleButton1);
    mHandler = new Handler();
    mToggleTick.retrigger=true;
    mToggleTick.doTimer();
    mClockTick.retrigger=true;
    mClockTick.doTimer();
    // Stop spinner after 5 seconds.
    mHandler.postDelayed(new Runnable() {
      
      @Override
      public void run() {
        mProgress.setVisibility(View.INVISIBLE);
      }
    }, 5000); 
    
    // Just tick up the progress bar.
    mHandler.postAtTime(new Runnable() {

      @Override
      public void run() {
        mHorizontal.incrementProgressBy(1);
        if (mHorizontal.getProgress()<mHorizontal.getMax()) {
          mHandler.postAtTime(this,"progress",SystemClock.uptimeMillis()+200);
        }
      }}, "progress", SystemClock.uptimeMillis()+200);
  }
  
  @Override
  protected void onDestroy() {
    // Shutting down the timers. Probably not needed in this demo.
    mToggleTick.retrigger=false; // Just setting a flag to stop Runnable from retrigger itself.
    mHandler.removeCallbacks(mClockTick);  // Actually removing the Runnable from the queue.
    mHandler.removeCallbacksAndMessages("progress"); // Use the token method to identify things to remove.
    super.onDestroy();
  }

  // Toggle the toggle button every second.
  private class ToggleTick implements Runnable {
    boolean retrigger=false;
    @Override
    public void run() {
      mToggle.setChecked(!mToggle.isChecked());
      if (retrigger) doTimer();
    }
    
    public void doTimer() {
      mHandler.postDelayed(this,500);
    }
  }
  
  private class ClockTick implements Runnable {
    public boolean retrigger;
    
    @Override
    public void run() {
      mRadio.setChecked(!mRadio.isChecked());
      mRadio.setText(mDateFmt.format(new Date()));
      if (retrigger) doTimer();
    }

    public void doTimer() {
      long nextTime = SystemClock.uptimeMillis() + 1000;  
      nextTime = (nextTime/1000) * 1000; // Force it to trigger on the actual second.
      mHandler.postAtTime(this, nextTime);
    }
  }

  
}
