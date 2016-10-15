package com.apress.ba3tp.mediaplayer;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ContentFragment extends Fragment implements OnSeekBarChangeListener  {
  
  private TextView mTitle;
  private TextView mInfo;
  private SeekBar mSeekBar;
  private TextView mDump;
  private MainActivity mMain;
  private SurfaceView mSurface;
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.content, null);
    SeekBar b = (SeekBar) v.findViewById(R.id.seekBar1);
    b.setOnSeekBarChangeListener(this);
    mSeekBar=b;
    mTitle=(TextView) v.findViewById(R.id.tvTitle);
    mInfo=(TextView) v.findViewById(R.id.tvInfo);
    mDump=(TextView) v.findViewById(R.id.tvDump);
    mMain=(MainActivity) getActivity();
    mSurface=(SurfaceView) v.findViewById(R.id.surfaceView1);
    return v;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      MediaPlayer mp = ((MainActivity) getActivity()).mPlayer;
      if (mp!=null) {
        mp.seekTo(progress);
      }
    }
  }

  public TextView getTitle() {
    return mTitle;
  }

  public TextView getInfo() {
    return mInfo;
  }

  public SeekBar getSeekBar() {
    return mSeekBar;
  }

  public TextView getDump() {
    return mDump;
  }


  public MainActivity getMain() {
    return mMain;
  }
  
  public SurfaceView getSurface() {
    return mSurface;
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
  }

}
