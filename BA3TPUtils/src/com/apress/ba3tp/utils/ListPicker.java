package com.apress.ba3tp.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ListPicker implements DialogInterface.OnClickListener {
  String mTitle;
  Context mContext;
  CharSequence[] mList;
  List<?> mObjects;
  private OnSelectedListener mDone;
  private AlertDialog mDialog;

  public ListPicker(Context context, List<?> list) {
    mContext = context;
    setList(list);
  }

  public void showDialog(OnSelectedListener onDone) {
    mDone = onDone;
    dismiss(); // Make sure existing dialog is cleared away.
    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
    b.setTitle(mTitle);
    b.setItems(mList, this);
    b.setNegativeButton("Cancel", null);
    mDialog = b.create();
    mDialog.show();
  }

  private void setList(List<?> list) {
    List<String> work = new ArrayList<String>(list.size());
    for (Object o : list) {
      work.add(o.toString());
    }
    mList = (CharSequence[]) work.toArray(new CharSequence[work.size()]);
    mObjects = list;
  }

  public void dismiss() {
    if (mDialog != null) {
      mDialog.dismiss();
      mDialog = null;
    }
  }

  public interface OnSelectedListener {
    public void onSelected(Object o);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    Object o = mObjects.get(which);
    mDone.onSelected(o);
  }
}
