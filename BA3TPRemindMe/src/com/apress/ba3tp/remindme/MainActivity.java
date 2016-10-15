package com.apress.ba3tp.remindme;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {
  public static final int PENDING_NOTIFY = 1; 
  public static final String WAKEUP_COMMAND = "com.apress.ba3tp.remindme.command";  
  public static final String WAKEUP_EXTRA_PHONE = "com.apress.ba3tp.remindme.phone";  
  public static final String WAKEUP_EXTRA_TEXT = "com.apress.ba3tp.remindme.text";  
  public static final String WAKEUP_EXTRA_ALARM_ID = "com.apress.ba3tp.remindme.alarm_id";
  public static final Uri REMIND_ME_URI = Uri.parse("alarm://com.apress.ba3tp.remindme"); 
  public static final int REMIND_ME_CHECK_TTS = 1002 ; 
  public static final int REMIND_ME_GET_PHONE = 1003 ; 
  EditText mPhone;
  EditText mText;
  DatePicker mDate;
  TimePicker mTime;
  SharedPreferences mPreferences;
  SQLiteDatabase mDb;
  boolean mUseTts = false;
  TextToSpeech mTts = null;
  static final SimpleDateFormat mSqlFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mPhone=(EditText) findViewById(R.id.ePhone);
    mText=(EditText) findViewById(R.id.eMessage);
    mDate=(DatePicker) findViewById(R.id.eDatePicker);
    mTime=(TimePicker) findViewById(R.id.eTimePicker);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mDb = (new AlarmDatabase(this)).getWritableDatabase(); 
    setVolumeControlStream(AudioManager.STREAM_MUSIC); // Allow the volume control to affect sound  
  }

  @Override
  protected void onPause() {
    savePreferences();
    super.onPause();
  }

  @Override
  protected void onResume() {
    loadPreferences();
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    mDb.close();
    super.onDestroy();
  }


  private void loadPreferences() {
    mPhone.setText(mPreferences.getString("phone", ""));
    mText.setText(mPreferences.getString("text",""));
    mUseTts = mPreferences.getBoolean("tts", false);
  }
  
  private void savePreferences() {
    SharedPreferences.Editor e = mPreferences.edit();
    e.putString("phone", mPhone.getText().toString());
    e.putString("text", mText.getText().toString());
    e.commit();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    Intent intent;
    switch (id) {
    case R.id.menuSearch:
      onSearchRequested();
      break;
    case R.id.menuSend:
      doSendSMS(); break;
    case R.id.menuSetAlarm:
      doSetAlarm();
      break;
    case R.id.menuShowAlarms:
      intent = new Intent(this,AlarmList.class);
      startActivity(intent);
      break;
    case R.id.menuPreferences:
      intent = new Intent(this,Preferences.class);
      startActivity(intent);
      break;
    case R.id.menuTestTTS:
      doTestTts();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onSearchRequested() {
    toast("Search");
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("vnd.android.cursor.item/phone");
    startActivityForResult(intent, REMIND_ME_GET_PHONE);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch(requestCode) {
    case REMIND_ME_GET_PHONE:
      if (data.hasExtra(Intent.EXTRA_PHONE_NUMBER)) {
        mPhone.setText(data.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
        SharedPreferences.Editor e = mPreferences.edit(); 
        e.putString("phone", mPhone.getText().toString());
        e.commit();
      }
      break;
    case REMIND_ME_CHECK_TTS: 
      if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
        // success, create the TTS instance
        mTts = new TextToSpeech(this,this);
       } else {
        // Go fetch needed data
        Intent installIntent = new Intent();
        installIntent.setAction(
            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivity(installIntent);
       }
      break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  void toast(Object msg) {
    Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show();
  }
  
  void doSendSMS() {
    SmsManager sms = SmsManager.getDefault();
    try {
      sms.sendTextMessage(mPhone.getText().toString(), null, mText.getText().toString(), null, null);
      toast("Sent.");
    } catch (Exception e) {
      toast(e);
    }
  }

  void doSetAlarm() {
    Calendar cal = GregorianCalendar.getInstance();
    cal.set(mDate.getYear(),mDate.getMonth(),mDate.getDayOfMonth(),mTime.getCurrentHour(),mTime.getCurrentMinute(),0);
    toast(cal.getTime());
    String message = mText.getText().toString();
    if (mUseTts) {
      message="#SPEAK "+message;
    }
    String phone = mPhone.getText().toString();

    ContentValues cv = new ContentValues();
    cv.put("phone", phone);
    cv.put("message", message);
    cv.put("trigger",mSqlFmt.format(cal.getTime()));
    cv.put("enabled", true);
    cv.put("status", "Pending");
    int lastId = (int) mDb.insert("alarms", null, cv);
    //int lastId = AlarmDatabase.getLastId(mDb);
    
    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    Intent intent = new Intent(this,WakeupHandler.class);
    intent.setData(Uri.withAppendedPath(REMIND_ME_URI, String.valueOf(lastId)));
    intent.putExtra(WAKEUP_COMMAND,"sms");
    intent.putExtra(WAKEUP_EXTRA_PHONE, phone);
    intent.putExtra(WAKEUP_EXTRA_TEXT, message);
    intent.putExtra(WAKEUP_EXTRA_ALARM_ID,lastId);
    PendingIntent operation = PendingIntent.getBroadcast(this, PENDING_NOTIFY, intent, 0 /*PendingIntent.FLAG_ONE_SHOT */);
    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), operation);
  }
  
  void doTestTts() {
    if (mTts!=null) {
      mTts.speak("Testing text to speech",TextToSpeech.QUEUE_FLUSH,null);
    } else {
      Intent checkIntent = new Intent();
      checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
      startActivityForResult(checkIntent, REMIND_ME_CHECK_TTS);
    }
  }

  @Override
  public void onInit(int status) {
//  mTts.setLanguage(Locale.US);
//  mTts.setPitch(0.8f);
//  mTts.setSpeechRate(1.2f);
    mTts.speak("TTS Loaded", TextToSpeech.QUEUE_FLUSH, null);
  }
}