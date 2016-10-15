package com.apress.ba3tp.exec;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.TextView;

public class ExecExample extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  public void onExec(View v) {
    TextView tv = (TextView) findViewById(R.id.mytext);
    try {
      Process p = Runtime.getRuntime().exec("ls /mnt/sdcard");
      BufferedReader r = new BufferedReader(new InputStreamReader(
          p.getInputStream()));
      StringBuilder builder = new StringBuilder();
      StringBuilderPrinter sp = new StringBuilderPrinter(builder);
      String line;
      while ((line = r.readLine()) != null) {
        sp.println(line);
      }
      tv.setText(builder.toString());
      r.close();
    } catch (Exception e) {
      tv.setText(e.getMessage());
    }
  }
}