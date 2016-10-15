package com.apress.ba3tp.opengl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private GameSurfaceGl surface;
  private TextView mText;
  private SeekBar mSeekBar;

  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); 
		surface = (GameSurfaceGl) findViewById(R.id.surface);
		mText = (TextView) findViewById(R.id.textView1);
    surface.setHandler(new Handler() {
      public void handleMessage(android.os.Message msg) {
        Bundle b = msg.getData();
        if (b!=null && b.containsKey("text")) {
          mText.setText(b.getString("text"));
        }
      }
    });
    mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
    mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
      
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }
      
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        surface.setZoom(progress);
        
      }
    });
	}

  // Resume and pause have to be passed through to the GL Surface
	
  @Override
	protected void onResume() {
		super.onResume();
		surface.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		surface.onPause();
	}

	public void clickLights(View v) {
	  ToggleButton btn = (ToggleButton) v;
	  surface.setLights(btn.isChecked());
	}
}