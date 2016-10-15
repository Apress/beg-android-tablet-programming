package com.apress.ba3tp.fragment;


import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ToggleButton;

public class TitleFragment extends Fragment {
  private View mContentView;
  private Button mButton;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
      mContentView = inflater.inflate(R.layout.main, null);
      mButton=(Button) mContentView.findViewById(R.id.button1);
      mButton.setOnClickListener(new OnClickListener() {
        
        @Override
        public void onClick(View v) {
          getActivity().getActionBar().setTitle("Button");
        }
      });
      View.OnClickListener btnlisten = new ButtonListener();
      mContentView.findViewById(R.id.checkBox1).setOnClickListener(btnlisten);
      return mContentView;
  }
  
  public void toggleClick(View view) {
    ToggleButton b = (ToggleButton) view;
    ActionBar a = getActivity().getActionBar(); 
    String s = (String) b.getText();
    a.setSubtitle(s);
  }
  
  private class ButtonListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      ActionBar ab = getActivity().getActionBar();
      if (v.getId() == R.id.checkBox1) {
        CheckBox chk = (CheckBox) v;
        ab.setTitle("Checkbox");
        if (chk.isChecked()) {
          ab.setSubtitle("Checked");
        } else {
          ab.setSubtitle("Not checked today.");
        }
      }
    }
  }
}

 
