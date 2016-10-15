package com.apress.ba3tp.testintents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ActivityTwo extends Activity {
  int mCount = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activitytwo);
  }

  public void onSwapBack(View v) {
    Intent intent = new Intent(this, ActivityOne.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtra(ActivityOne.COUNT, mCount + 1);
    startActivity(intent);
  }
  
  public void onFinish(View v) {
    finish();
  }

  @Override
  public void onStart() {
    super.onStart();
    Intent intent = getIntent(); // Get the intent that started this.
    mCount = intent.getIntExtra(ActivityOne.COUNT, 0);
    Toast.makeText(this,"Called "+mCount+" times so far.",Toast.LENGTH_SHORT).show();
  }

}
