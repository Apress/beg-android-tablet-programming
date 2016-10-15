package com.apress.ba3tp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MessageBox {
  private AlertDialog mDialog;
  private Context mContext;
  private String mTitle = "Caption"; 
  
  public MessageBox(Context context) {
    mContext=context;  
  }

  public MessageBox(Context context, String title) {
    mContext=context;  
    mTitle = title;
  }
  
  public void clearDialog() {
    if (mDialog!=null) {
      mDialog.dismiss();
      mDialog=null;
    }
  }
  
  public void showMessage(Object message) {
    showMessage(mTitle,message);
  }
  
  public void showMessage(String caption, Object message) {
    promptMessage(caption,message,"Ok",new DialogInterface.OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    }, caption, null);
    
  }
  
  public void promptMessage(String caption, Object message, DialogInterface.OnClickListener onPositive) {
    promptMessage(caption,message,"Yes",onPositive,"No",new DialogInterface.OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
        
  }

  public void promptMessage(String caption, Object message, String positive, DialogInterface.OnClickListener onPositive, String negative, DialogInterface.OnClickListener onNegative) {
    clearDialog();
    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
    b.setTitle(caption);
    b.setMessage(message.toString());
    if (positive==null) positive="Yes";
    if (onPositive!=null) b.setPositiveButton(positive, onPositive);
    if (negative==null) negative="No";
    if (onNegative!=null) b.setNegativeButton(negative, onNegative);
    mDialog=b.create();
    mDialog.show();
  }

}
