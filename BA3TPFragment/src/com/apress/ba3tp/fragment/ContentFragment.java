package com.apress.ba3tp.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContentFragment extends Fragment {
  private View mContentView;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
      mContentView = inflater.inflate(R.layout.other, null);
      return mContentView;
  }      
}
