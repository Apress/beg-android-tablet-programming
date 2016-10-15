package com.apress.ba3tp.remindme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
  private SharedPreferences mPreferences;

  @Override
  public void onReceive(Context context, Intent intent) {
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    if (!mPreferences.getBoolean("tts", false))
      return;
    Bundle bundle = intent.getExtras();
    Object messages[] = (Object[]) bundle.get("pdus");
    for (int n = 0; n < messages.length; n++) {
      SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) messages[n]);
      String body = smsMessage.getMessageBody();
      if (body.startsWith("#SPEAK")) {
        Intent service = new Intent(context, SmsReceiveService.class);
        service.putExtra("tts", body.substring(6).trim());
        context.startService(service);
      }
    }
  }
}
