package com.apress.ba3tp.testintents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ActivityOne extends Activity {
  public static final String COUNT = "com.apress.ba3tp.testIntents.count";
  int mCount = 0; // Track how many times we've been called, via the Intent.

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activityone);
  }

  public void onSwap(View v) {
    Intent intent = new Intent(this, ActivityTwo.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtra(COUNT, mCount + 1);
    startActivity(intent);
  }

  @Override
  public void onStart() {
    super.onStart();
    Intent intent = getIntent(); // Get the intent that started this.
    mCount = intent.getIntExtra(COUNT, 0);
    Toast.makeText(this, "Called " + mCount + " times so far.",
        Toast.LENGTH_SHORT).show();
  }

}