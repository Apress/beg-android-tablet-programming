package com.apress.ba3tp.mediaplayer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends Activity {

  ContentFragment mContent;
  ListFragment mList;
  AlertDialog mDialog;
  String mTitle;

  @Override
  protected void onDestroy() {
    clearMedia();
    super.onDestroy();
  }

  MediaPlayer mPlayer;
  private Uri mCurrentMedia;

  private static enum MenuId {
    PLAY, STOP, MEDIATYPE, LOGO;
    public int getId() {
      return ordinal() + Menu.FIRST;
    } 
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    FragmentManager fm = getFragmentManager();
    mList = (ListFragment) fm.findFragmentById(R.id.frag_list);
    mContent = (ContentFragment) fm.findFragmentById(R.id.frag_content);
    getActionBar().setDisplayUseLogoEnabled(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MenuId.PLAY.getId(), Menu.NONE, "Play")
        .setIcon(android.R.drawable.ic_media_play)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    menu.add(Menu.NONE, MenuId.STOP.getId(), Menu.NONE, "Stop")
        .setIcon(android.R.drawable.ic_media_pause)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    menu.add(Menu.NONE, MenuId.MEDIATYPE.getId(), Menu.NONE, "Source")
    .setIcon(android.R.drawable.ic_menu_preferences)
    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    menu.add(Menu.NONE, MenuId.LOGO.getId(), Menu.NONE, "Logo/Icon")
    .setIcon(android.R.drawable.ic_menu_crop)
    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == MenuId.PLAY.getId()) {
      playMedia();
    } else if (id == MenuId.STOP.getId()) {
      stopMedia();
    } else if (id == MenuId.MEDIATYPE.getId()) {
      showDialog(1);
      toast("Dialog");
    } else if (id == MenuId.LOGO.getId()) { // Toggle between icon and logo
      boolean isLogo = ((getActionBar().getDisplayOptions() & ActionBar.DISPLAY_USE_LOGO)!=0);
      getActionBar().setDisplayUseLogoEnabled(!isLogo);
    }
    return super.onOptionsItemSelected(item);
  }

  private void stopMedia() {
    clearMedia();
  }

  private void clearMedia() {
    if (mPlayer != null) {
      mPlayer.stop();
      mPlayer.release();
      mPlayer = null;
    }
    showVideo(false);
  }

  private void showVideo(boolean onOff) {
    mContent.getSurface().setVisibility(onOff ? View.VISIBLE : View.GONE);
    mContent.getDump().setVisibility(onOff ? View.GONE : View.VISIBLE);
  }
  
  private void playMedia() {
    clearMedia();
    if (getSelectedMedia().toString().contains("/video/")) {
      showVideo(true);
      mPlayer = MediaPlayer.create(this, getSelectedMedia(),mContent.getSurface().getHolder());
    } else {
      showVideo(false);
      mPlayer = MediaPlayer.create(this, getSelectedMedia());
    }
    if (mPlayer == null)
      toast("Unable to open.\n" + getSelectedMedia());
    else {
      mPlayer.start();
      getActionBar().setSubtitle(mTitle);
      startProgressThread();
    }
  }

  private void startProgressThread() {

    Runnable _progressUpdater = new Runnable() {
      @Override
      public void run() {
        SeekBar progressBar = getSeekBar();
        while (mPlayer != null && mPlayer.isPlaying()) {
          try {
            int current = 0;
            int total = mPlayer.getDuration();
            progressBar.setMax(total);
            progressBar.setIndeterminate(false);

            while (mPlayer != null && current < total) {
              Thread.sleep(1000); // Update once per second
              current = mPlayer.getCurrentPosition();
              // Removing this line, the track plays normally.
              progressBar.setProgress(current);
            }
          } catch (Exception e) {
            break;
          }
        }
      }
    };
    Thread thread = new Thread(_progressUpdater);
    thread.start();
  }

  private void toast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  private Uri getSelectedMedia() {
    return mCurrentMedia;
  }

  public void setSelectedMedia(Uri uri) {
    mCurrentMedia = uri;
    mTitle=uri.toString();
  }

  private String getString(Cursor c, String fieldname) {
    int column = c.getColumnIndex(fieldname);
    return (column >= 0) ? c.getString(column) : "";
  }

  private long getLong(Cursor c, String fieldname) {
    int column = c.getColumnIndex(fieldname);
    return (column >= 0) ? c.getLong(column) : 0;
  }

  public void setSelectedMedia(Cursor c) {
//    String data = getString(c, Media.DATA);
//mCurrentMedia = Uri.fromFile(new File(data));
    mCurrentMedia = Uri.withAppendedPath(mList.getMediaSource(), getString(c,Media._ID));
    String title = getString(c, Media.TITLE);
    mTitle = title;
    String info = "Artist " + getString(c, Media.ARTIST) + "\nAlbum "
        + getString(c, Media.ALBUM) + "\nLength " + getLong(c, Media.DURATION)
        / 1000 + " seconds";
    mContent.getTitle().setText(title);
    mContent.getInfo().setText(info);
    mContent.getDump().setText(mCurrentMedia+"\n"+dumpCursor(c));
  }

  private CharSequence dumpCursor(Cursor c) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < c.getColumnCount(); i++) {
      b.append(c.getColumnName(i) + " : " + c.getString(i) + "\n");
    }
    return b.toString();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case 1:
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Select Media Source");
      String[] options = { "External Audio", "Internal Audio","External Video","Internal Video" };
      builder.setItems(options, new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (which==0) mList.setMediaSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
          else if (which==1) mList.setMediaSource(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
          else if (which==2) mList.setMediaSource(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
          else if (which==3) mList.setMediaSource(MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
      });
      mDialog = builder.create();
      return mDialog;
    }
    return null;
  }

  SeekBar getSeekBar() {
    return mContent.getSeekBar();
  }
}