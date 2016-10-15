package com.apress.ba3tp.remindme;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;
import android.widget.Toast;

public class WakeupHandler extends BroadcastReceiver {
  public static final int NOTIFICATION_ID = 1001;
  private Context mContext;
  private SQLiteDatabase mDb;

  @Override
  public void onReceive(Context context, Intent intent) {
    mDb = (new AlarmDatabase(context)).getWritableDatabase();
    mContext = context;
    String cmd = intent.getStringExtra(MainActivity.WAKEUP_COMMAND);
    if (cmd == null) {
      toast("No command");
    } else {
      toast(cmd);
      int alarm_id = intent.getIntExtra(MainActivity.WAKEUP_EXTRA_ALARM_ID, 0);
      if (cmd.equals("sms")) {
        doSendSms(intent.getStringExtra(MainActivity.WAKEUP_EXTRA_PHONE),intent.getStringExtra(MainActivity.WAKEUP_EXTRA_TEXT),
            alarm_id);
      } else if (cmd.equals("sent") || cmd.equals("delivered")) {
        int ret = getResultCode();
        doNotify("SMS "+cmd+" "+errmsg(ret));
        updateStatus(alarm_id,cmd+" "+errmsg(ret));
      }
    }
    mDb.close();
  }

  private String errmsg(int ret) {
    switch (ret) {
    case SmsManager.RESULT_ERROR_GENERIC_FAILURE: return "Failure";
    case SmsManager.RESULT_ERROR_NO_SERVICE: return "No Service";
    case SmsManager.RESULT_ERROR_NULL_PDU: return "Null PDU";
    case SmsManager.RESULT_ERROR_RADIO_OFF: return "Radio Off";
    case 0: return "OK";
    case -1: return "Done";
    default: return "Result="+ret;
    }
  }
  
  private Intent makeIntent(String msg, int id) {
    Intent intent = new Intent(mContext,this.getClass());
    intent.putExtra(MainActivity.WAKEUP_COMMAND, msg);
    intent.putExtra(MainActivity.WAKEUP_EXTRA_ALARM_ID,id);
    return intent;
  }
  
  private void doSendSms(String phone, String text, int id) {
    SmsManager sms = SmsManager.getDefault();
    try {
      doNotify("Alarm triggered");
      Intent sendIntent = makeIntent("sent",id);
      Intent deliveredIntent = makeIntent("delivered",id);
      PendingIntent sent = PendingIntent.getBroadcast(mContext, 0, sendIntent, PendingIntent.FLAG_ONE_SHOT);
      PendingIntent delivered = PendingIntent.getBroadcast(mContext, 0, deliveredIntent, PendingIntent.FLAG_ONE_SHOT);
      sms.sendTextMessage(phone, null, text, sent, delivered);
      updateStatus(id,"queued");      
    } catch (Exception e) {
      toast(e);
    }
  }

  
  void doNotify(String msg) {
    NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notify =new Notification(R.drawable.icon,msg,System.currentTimeMillis());
    Intent notificationIntent = new Intent(mContext, MainActivity.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
    notify.setLatestEventInfo(mContext, "RemindMe", msg, contentIntent);
    notify.flags |= Notification.FLAG_AUTO_CANCEL;
    nm.notify(NOTIFICATION_ID, notify);
  }
  
  void updateStatus(int alarm_id, String msg) {
    ContentValues cv = new ContentValues();
    cv.put("status", msg);
    cv.put("lastupdate", MainActivity.mSqlFmt.format(new Date()));
    mDb.update("alarms",cv,"_id="+alarm_id,null);
  }

  void toast(Object msg) {
    Toast.makeText(mContext, msg.toString(), Toast.LENGTH_SHORT).show();
  }

}
