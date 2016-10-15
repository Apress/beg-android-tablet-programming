package com.apress.ba3tp.media1;

import java.io.File;

import com.apress.ba3tp.media1.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
  TextView mDisplay;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDisplay=(TextView) findViewById(R.id.eDisplay);
    }
    
    public void onButtonClick(View v) {
      try {
        File media = new File("/sdcard/MP3/canon1.mp3");
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(media),"audio/mpeg");
        startActivity(intent);
      } catch (Exception e) {
        display(e);
      }
    }
    // Show a message on the screen
    public void display(Object msg) {
      mDisplay.setText(msg.toString());
    }
    // Add a line to the existing message
    public void addln(Object msg) {
      String s=(String) mDisplay.getText();
      if (s.equals("")) s=msg.toString();
      else s+="\n"+msg.toString();
      mDisplay.setText(s);
    }
}

    