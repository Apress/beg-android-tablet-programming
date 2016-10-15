package com.apress.ba3tp.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class FilePicker implements DialogInterface.OnClickListener {
  File mBase;
  File mFile;
  String mExtension;
  String mTitle;
  Context mContext;
  CharSequence[] mList;
  private OnSelectedListener mDone;
  private AlertDialog mDialog;
  
  public FilePicker(Context context, File basepath, String title, String extension) {
    mContext=context;
    mBase=basepath;
    mTitle=title;
    mExtension=extension;
  }
  
  public void showDialog(OnSelectedListener onDone) {
    mDone=onDone;
    dismiss(); // Make sure existing dialog is cleared away.
    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
    b.setTitle(mTitle);
    setList(mBase,mExtension);
    b.setItems(mList,this);
    b.setNegativeButton("Cancel", null);
    mDialog = b.create();
    mDialog.show();
  }
  
  private void setList(File mBase, String extension) {
    String ext = "."+extension.toLowerCase();
    List<String> work = new ArrayList<String>();
    File[] list = mBase.listFiles();
    for (File f:list) {
      if (f.isHidden()) continue;
      if (f.isDirectory()) work.add(f.getName()+"/");
      else if (f.getName().toLowerCase().endsWith(ext)) work.add(f.getName());
    }
    Collections.sort(work,new Comparator<String>() {

      @Override
      public int compare(String s1, String s2) {
        return s1.compareToIgnoreCase(s2);
      }
    });
    if (mBase.getParent()!=null) work.add(0,"..");
    mList = (CharSequence[]) work.toArray(new CharSequence[work.size()]);
  }
  
  public void dismiss() {
    if (mDialog!=null) {
      mDialog.dismiss();
      mDialog=null;
    }
  }
  
  public interface OnSelectedListener {
    public void onSelected(File file);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
      String name = (String) mList[which];
      if (name.equals("..")) mFile = mBase.getParentFile();
      else {
        if (name.endsWith("/")) name=name.substring(0, name.length()-1);
        mFile = new File(mBase, name);
      }
      if (mFile.isDirectory()) reloadList(mFile);
      else mDone.onSelected(mFile);
  }

  private void reloadList(File mFile) {
    mBase=mFile;
    showDialog(mDone);
  }
}
