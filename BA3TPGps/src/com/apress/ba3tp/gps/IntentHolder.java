package com.apress.ba3tp.gps;

import android.app.PendingIntent;

// This class just holds a static instance of a Pending Intent.
public class IntentHolder {
  private PendingIntent mIntent;
  private static IntentHolder mInstance=null;
  
  public static IntentHolder getInstance() {
    if (mInstance==null) {
      mInstance=new IntentHolder();
    }
    return mInstance;
  }

  public static boolean hasIntent() {
    return getInstance().getIntent()!=null;
  }
  
  public PendingIntent getIntent() {
    return mIntent;
  }

  public void setIntent(PendingIntent intent) {
    this.mIntent = intent;
  }

}
