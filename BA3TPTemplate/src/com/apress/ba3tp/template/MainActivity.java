package com.apress.ba3tp.template;

import android.app.Activity;
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
      display("Button pressed.");
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

    