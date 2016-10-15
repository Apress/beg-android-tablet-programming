package com.apress.ba3tp.hello;

import com.apress.ba3tp.hello.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Hello extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  public void clickHello(View view) {
    Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
  }
}