package com.apress.ba3tp.remindme;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class SmsReceiveService extends Service implements OnInitListener {
  TextToSpeech mTts;
  boolean mTtsStarted = false;
  Queue<String> mMessages = new LinkedList<String>();

  @Override
  public void onCreate() {
    super.onCreate();
    mTtsStarted=false;
    mTts = new TextToSpeech(this,this);
  }

  @Override
  public void onDestroy() {
    mTts.shutdown();
    mTts=null;
    mTtsStarted=false;
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String msg = intent.getStringExtra("tts");
    if (msg!=null) {
      synchronized(mMessages) {
        mMessages.add(msg);
      }
      if (mTtsStarted) doSpeech();
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    // Not using this here.
    return null;
  }

  @Override
  public void onInit(int status) {
    mTtsStarted=true;
    doSpeech();    
  }

  void doSpeech() {
    String msg;
    synchronized(mMessages) {
    while ((msg=mMessages.poll())!=null) {
      mTts.speak(msg, TextToSpeech.QUEUE_ADD, null);
    }
    }
  }

}
