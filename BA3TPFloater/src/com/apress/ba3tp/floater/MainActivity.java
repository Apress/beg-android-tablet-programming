package com.apress.ba3tp.floater;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
  GameSurface mGame;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mGame = (GameSurface) findViewById(R.id.gameSurface);
        mGame.setTextView((TextView) findViewById(R.id.textView1));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    public void pauseClick(MenuItem v) {
      mGame.togglePause();
    }

    public void clearClick(MenuItem v) {
      mGame.getThread().clearAll();
    }
}