package com.apress.ba3tp.mediaplayer;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ListFragment extends android.app.ListFragment {
  private Uri mMediaSource = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
  private SimpleCursorAdapter mAdapter;
  
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Cursor c = mAdapter.getCursor();
    c.moveToPosition(position);
    ((MainActivity) getActivity()).setSelectedMedia(c);
  }

  @Override
  public void onStart() { 
    super.onStart();
    String[] columns = { MediaStore.MediaColumns.TITLE,MediaStore.MediaColumns.DISPLAY_NAME};
    int[] to = { android.R.id.text1,android.R.id.text2 };
    mAdapter=new SimpleCursorAdapter(getActivity(),
        android.R.layout.two_line_list_item, getCursor(), columns, to, 0);
    setListAdapter(mAdapter);
  }

  public Cursor getCursor() {
    Activity a = getActivity();
    return a.managedQuery(mMediaSource,
        null, null, null, MediaStore.Audio.Media.TITLE);
  }
  
  public void setMediaSource(Uri mMediaSource) {
    this.mMediaSource = mMediaSource;
    Cursor c = getCursor();
    mAdapter.swapCursor(c).close();
  }

  public Uri getMediaSource() {
    return mMediaSource;
  }
}
