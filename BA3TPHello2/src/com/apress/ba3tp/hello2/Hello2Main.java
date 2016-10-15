package com.apress.ba3tp.hello2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Hello2Main extends Activity {
  EditText mEditText1;
  SharedPreferences mPreferences;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mEditText1 = (EditText) findViewById(R.id.editText1);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this); 
  }

  @Override
  public void onResume() {
    super.onResume();
    mEditText1.setText(mPreferences.getString("myedit", "EMPTY"));    
  }
  
  public void onPause() {
    super.onPause();
    Editor e = mPreferences.edit();
    e.putString("myedit", mEditText1.getText().toString());
    e.commit();
  }
  
  public void onPressClick(View v) {
    Toast.makeText(this, "You pressed me!", Toast.LENGTH_SHORT).show();
  }

  public void onPressMore(View v) {
    TextView tv = (TextView) findViewById(R.id.textview);
    tv.setText("I've been pressed too.");
  }
  
  public void onBrowseClick(View v) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(Uri.parse("http://google.com"));
    startActivity(intent);
  }
  
  public void onPdfClick(View v) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.parse("file:///scdard/Download/sample.pdf"), "application/pdf");
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
    }
  }
}