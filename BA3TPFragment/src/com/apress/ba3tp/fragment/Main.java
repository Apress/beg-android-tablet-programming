package com.apress.ba3tp.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Main extends Activity {

  private static enum MenuId {
    SHARE, MAIL,BACK,FORWARD;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragholder);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MenuId.SHARE.getId(), Menu.NONE, "Share")
        .setIcon(android.R.drawable.ic_menu_share);
    menu.add(Menu.NONE, MenuId.MAIL.getId(), Menu.NONE, "Mail")
    .setIcon(android.R.drawable.ic_menu_send);
    menu.add(Menu.NONE, MenuId.BACK.getId(), Menu.NONE, "Back")
    .setIcon(android.R.drawable.ic_media_rew)
    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    menu.add(Menu.NONE, MenuId.FORWARD.getId(), Menu.NONE, "Fwd")
    .setIcon(android.R.drawable.ic_media_ff)
    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
    if (id==MenuId.FORWARD.getId()) {
      doForward();
    } else if (id==MenuId.BACK.getId()) {
      doBack();
    }
    return super.onOptionsItemSelected(item);
  }
  
  private RatingBar getRating() {
    Fragment f = getFragmentManager().findFragmentById(R.id.frag_content);
    return (RatingBar) f.getView().findViewById(R.id.ratingBar1);
  }
  
  private void doForward() {
    RatingBar r = getRating();
    r.setProgress(Math.min(r.getMax(), r.getProgress()+1));
  }
  
  private void doBack() {
    RatingBar r = getRating();
    r.setProgress(Math.max(0, r.getProgress()-1));
  }

  public void toggleClick(View view) {
    ToggleButton b = (ToggleButton) view;
    ActionBar a = getActionBar(); 
    String s = (String) b.getText();
    a.setSubtitle(s);
  }


}