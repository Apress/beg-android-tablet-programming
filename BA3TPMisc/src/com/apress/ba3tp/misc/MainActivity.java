package com.apress.ba3tp.misc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onTimersClick(View v) {
      Intent intent = new Intent(this,TimersActivity.class);
      startActivity(intent);
    }
    
    public void onAsynch(View v) {
      Intent intent = new Intent(this,AsynchActivity.class);
      startActivity(intent);
    }

    public void onDownload(View v) {
      Intent intent = new Intent(this,DownloadActivity.class);
      startActivity(intent);
    }
    
    public void onAnimation(View v) {
      Intent intent = new Intent(this,AnimateActivity.class);
      startActivity(intent);
    }

    public void onUsb(View v) {
      Intent intent = new Intent(this,UsbActivity.class);
      startActivity(intent);
    }
}