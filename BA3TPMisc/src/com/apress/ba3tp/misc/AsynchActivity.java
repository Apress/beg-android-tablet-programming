package com.apress.ba3tp.misc;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AsynchActivity extends Activity {

  TextView mProgress;
  Button mOk;
  MyAsynchTask mTask;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.asynch);
    mProgress = (TextView) findViewById(R.id.eProgress);
    mOk = (Button) findViewById(R.id.btnOk);
  }
  
  public void clickOK(View v) {
    v.setEnabled(false); // Stop doubling stuff up.
    mTask = new MyAsynchTask(this);
    mTask.execute("/sdcard");
  }
  
  public void clickCancel(View v) {
    if (mTask!=null) {
      mTask.cancel(true);
    }
  }
  
  // Cunning bit of code to run something in the main thread.
  public static void runInMainThread(Context context,final Runnable task) {
    Handler handler = new Handler(context.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        task.run();
      }
    });
  }
  
  public boolean askProceed(final Context context) {
    
    final FutureResult mResult = new FutureResult();
    
    runInMainThread(context, new Runnable() {
    @Override
    public void run() {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle("Demo Dialog");
      builder.setMessage("This will wait until a response is offered.");

      DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          boolean result = (which==DialogInterface.BUTTON_POSITIVE);
          mResult.set(result);
          dialog.dismiss();
        }
      };
      builder.setNegativeButton("No", buttonListener);
      builder.setPositiveButton("Yes", buttonListener);

      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          mResult.set(false);
          dialog.dismiss();
        }
      });
      builder.show();
    }
  });
    try {
      return mResult.get();
    } catch (InterruptedException e) {
      return false;
    }
  }
  
  class MyAsynchTask extends AsyncTask<String, String, String> {

    Context mContext;
    
    MyAsynchTask(Context context) {
      mContext = context;
    }
    @Override
    protected void onCancelled(String result) {
      mOk.setEnabled(true);
      mProgress.setText("Cancelled:"+result);
      super.onCancelled(result);
    }

    @Override
    protected void onPostExecute(String result) {
      mOk.setEnabled(true);
      mProgress.setText("Done: "+result);
      super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
      mProgress.setText("Starting...");
      super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(String... values) {
      if (values.length>0) {
        mProgress.setText(values[0]);
      }
    }

    private void recurseFiles(File file, long totals[]) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        if (files==null) return;
        publishProgress(file.getName());
        for (File f: files) {
          if (isCancelled()) break; // Breaking out.
          recurseFiles(f,totals);
        }
      } else {
        totals[0] += 1;
        totals[1] += file.length();
      }
    }
    
    @Override
    protected String doInBackground(String... params) {
      // This is where all the work actually happens.
      long totals[] = new long[2];
      boolean ret=false;
      if (params.length<1) {
        return "No parameters supplied.";
      }
      try {
        File start = new File(params[0]);
        recurseFiles(start,totals);
        ret=askProceed(mContext);
      } catch (Exception e) {
        return e.toString();
      }
      return "Files: "+totals[0]+" Size: "+totals[1]+" ret="+ret;
    }
  }

  public class FutureResult implements Future<Boolean> {

    private final CountDownLatch mLatch = new CountDownLatch(1);
    private volatile Boolean mResult;
    
    public void set(Boolean result) {
      mResult=result;
      mLatch.countDown();
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }
    
    @Override
    public Boolean get() throws InterruptedException {
      mLatch.await();
      return mResult;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit)
        throws InterruptedException {
      mLatch.await(timeout,unit);
      return mResult;
    }
    
    @Override
    public boolean isCancelled() {
      return false;
    }
    @Override
    public boolean isDone() {
      return false;
    }
  }
}