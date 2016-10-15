package com.apress.ba3tp.floater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

/*
 * Custom font: Orbitron Black, by Matt McInerney.
 * Source: The League of Movable Type
 * http://www.theleagueofmoveabletype.com/fonts/12-orbitron
 */
public class GameSurface extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {
  private FloaterThread mThread;
  private TextView mTextView;
  private long universeClock = 0;
  private long lastInfo = 0;
  
  public static final int TRAIL_AGE = 6000;
  public static final long START_SPEED =100;
  public static final long GRAVITY =8000;
  public static final long UPDATE_RATE = 500; // Delay in MS between text updates
  

  class Thingy {
    double x;
    double y;
    double deltax; 
    double deltay;
    int radius = 8;
    long timestamp;
    protected int color = Color.GREEN;
    protected Paint paint = new Paint();
    
    Thingy() {};
    Thingy(float x, float y) {
      this.x=x;
      this.y=y;
      setColor(Color.GREEN);
      timestamp=universeClock;
    }
    
    double distanceTo(Thingy dest) {
      return Math.hypot(x-dest.x, y-dest.y);
    }
    
    double speed() {
      return Math.hypot(deltax, deltay);
    }
    
    double angle() {
      return Math.toDegrees(Math.atan2(deltay, deltax));
    }
    
    void draw(Canvas c) {
      c.drawCircle((float) x, (float) y, radius,paint);
    }
    
    public int getColor() {
      return color;
    }
    
    public void setColor(int color) {
      paint.setColor(color);
    }
    
  }
  
  class TrailSegment {
    int x1,y1;
    int x2,y2;
    long age;
  }
  
  class ThingyTrail extends Thingy {
    List<TrailSegment> mList = new ArrayList<TrailSegment>();
    int lastx = -1;
    int lasty = -1;
    int maxAge = TRAIL_AGE;
    
    boolean stateChange = true;
    
    @Override
    void draw(Canvas c) {
      if ((int) x != lastx || (int) y != lasty) {
        TrailSegment ts = new TrailSegment();
       if (!stateChange) {
         ts.x1 = lastx;
         ts.y1 = lasty;
         ts.x2 = (int) x;
         ts.y2 = (int) y;
         ts.age = universeClock;
         mList.add(ts);
       }
       lastx = (int) x;
       lasty = (int) y;
       stateChange=false;
      }
      paint.setStrokeWidth(8);
      Iterator<TrailSegment> segIt = mList.iterator();
      while (segIt.hasNext()) {
        TrailSegment seg = segIt.next();
        if (seg.age+maxAge<universeClock) {
          segIt.remove();
        }
        int alpha = (int) ((maxAge - (universeClock-seg.age))*255/maxAge);
        if (alpha<0) alpha=0;
        paint.setAlpha(alpha);
        c.drawLine(seg.x1, seg.y1, seg.x2, seg.y2, paint);
      }
      paint.setAlpha(255);
      super.draw(c);
    }
  }
  
  class FloaterThread extends Thread {
    private SurfaceHolder mSurfaceHolder;
    private Handler mHandler;
    private Context mContext;
    private List<Thingy> attractors = new ArrayList<Thingy>();
    private boolean mRun = false;
    private long mLastTime;
    private ThingyTrail mTarget = new ThingyTrail();
    private boolean mStarted = false;
    private int mWidth;
    private int mHeight;
    private boolean mPaused = false;
    private Typeface myFont;
    private Paint mFontPaint;

    public FloaterThread(SurfaceHolder surfaceHolder, Context context,
        Handler handler) {
      // get handles to some important objects
      mSurfaceHolder = surfaceHolder;
      mHandler = handler;
      mContext = context;
    }

    public void setRunning(boolean onOff) {
      if (mRun != onOff && mRun == true)
        mLastTime = System.currentTimeMillis();
      mRun = onOff;
    }

    
    public boolean isPaused() {
      return mPaused;
    }

    public synchronized void setPaused(boolean paused) {
      this.mPaused = paused;
      if (!paused) mLastTime=System.currentTimeMillis();
    }

    public synchronized void setAttractor(int index, int ax, int ay) {
      while (attractors.size()<=index) attractors.add(null); 
      Thingy attractor= new Thingy(ax,ay);
      attractor.setColor(Color.RED);
      attractors.set(index, attractor);
    }
    
    public synchronized void clearAll() {
      attractors.clear();
    }
    
    private void doDraw(Canvas canvas) {
      canvas.drawColor(Color.BLACK);
      mTarget.draw(canvas);
      for (Thingy attractor : attractors) {
        if (attractor != null) {
          attractor.draw(canvas);
        }
      }
      if (mPaused) {
        showText(canvas, "Paused");
      }
      long now = System.currentTimeMillis();
      if (now-lastInfo>UPDATE_RATE) { // Only send updates every half a second.
        lastInfo=now;
        if (mPaused) {
          sendMessage("Paused");
          } else {
            String info = String.format("Speed %1.0f Angle %1.0f",mTarget.speed(),mTarget.angle());
            sendMessage(info);
          }
        }
    }

    private void doMove() {
      if (mPaused) return;
      long now = System.currentTimeMillis();
      if (!mStarted) {
        mTarget.x=0;
        mTarget.y=mHeight/2;
        mTarget.setColor(Color.GREEN);
        mLastTime = now;
        mStarted=true;
        mTarget.deltax=START_SPEED; // Pixels/second
        mTarget.deltay=0;
      }
      double travelled = (double) (now-mLastTime)/1000;
      universeClock += (now-mLastTime);
      for (int i=0; i<attractors.size(); i++) {
        Thingy attractor = attractors.get(i);
        if (attractor != null) {
            double dist = mTarget.distanceTo(attractor);
            if (dist > 0) {
              double dx = (attractor.x - mTarget.x) / dist;
              double dy = (attractor.y - mTarget.y) / dist;
              double acceleration = (GRAVITY / dist) * travelled;
              mTarget.deltax += dx * acceleration;
              mTarget.deltay += dy * acceleration;
          }
        }
      }
      mTarget.x += (mTarget.deltax * travelled);
      mTarget.y += (mTarget.deltay * travelled);
      if (mTarget.x < 0) {
        mTarget.x = mWidth;
        mTarget.stateChange = true;
      } else if (mTarget.x > mWidth) {
        mTarget.x = 0;
        mTarget.stateChange = true;
      }
      if (mTarget.y < 0) {
        mTarget.y = mHeight;
        mTarget.stateChange = true;
      } else if (mTarget.y > mHeight) {
        mTarget.y = 0;
        mTarget.stateChange = true;
      }
      mLastTime=System.currentTimeMillis();
    }

    private void showText(Canvas c,String msg) {
      if (myFont == null) {
        myFont = Typeface.createFromAsset(mContext.getAssets(),
            "orbitron-black.ttf");
        mFontPaint = new Paint();
        mFontPaint.setTypeface(myFont);
        mFontPaint.setColor(Color.YELLOW);
        mFontPaint.setAlpha(128);
        mFontPaint.setShadowLayer(4, 8, 8, Color.BLUE);
        mFontPaint.setTextSize(64);
      }
      Rect bounds = new Rect();
      mFontPaint.getTextBounds(msg, 0, msg.length(), bounds);
      int x =  (mWidth-bounds.width())/2;
      int y = (mHeight-(bounds.height()))/2 - (int) mFontPaint.ascent();
      c.drawText(msg, x, y, mFontPaint);
  }
      
    
    // The textview belongs to another Thread (the Main UI thread
    // We're not allowed to update it directly.
    // There, we make use of the message Handler we prepared earlier.
    private void sendMessage(Object info) {
      Message msg = mHandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("info", info.toString());
      msg.setData(b);
      mHandler.sendMessage(msg);
    }

    public void setDimensions(int width, int height) {
      mWidth = width;
      mHeight = height;
    }
    

    @Override
    public void run() {
      while (mRun) {
        Canvas c = null;
        try {
          c = mSurfaceHolder.lockCanvas(null);
          synchronized (mSurfaceHolder) {
            doMove();
            doDraw(c);
          }
        } finally {
          if (c != null) {
            mSurfaceHolder.unlockCanvasAndPost(c);
          }
        }
      }
    }
  }

  public GameSurface(Context context, AttributeSet attrs) {
    super(context, attrs);
    // register our interest in hearing about changes to our surface
    SurfaceHolder holder = getHolder();
    holder.addCallback(this);
    setOnTouchListener(this);
    // Custom font.
    // create thread; it's started in surfaceCreated()
    // Note that the Handler we're constructing here runs in the original thread.
    mThread = new FloaterThread(holder, context, new Handler() {
      @Override
      public void handleMessage(Message m) {
        Bundle b = m.getData();
        if (b!=null) {
          if (mTextView!=null) {
            String info = b.getString("info");
            mTextView.setText(info);
          }
        }
      }
    });

    setFocusable(true); // make sure we get key events
  }
  
  public FloaterThread getThread() {
    return mThread;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    mThread.setDimensions(width, height);

  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    mThread.setRunning(true);
    try {
      mThread.start();
    } catch (Exception e) { // Thread may still be running...
      Log.w("floater",e);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    mThread.setRunning(false);
    boolean retry = true;
    // Hang about until thread has given up.
    while (retry) {
      try {
        mThread.join();
        retry = false;
      } catch (InterruptedException e) {
      }
    }
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    int action = event.getActionMasked();
    if (action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_MOVE || action==MotionEvent.ACTION_POINTER_DOWN) {
      for (int i=0; i<event.getPointerCount(); i++) {
        mThread.setAttractor(i,(int)event.getX(i),(int) event.getY(i));
      }
    }
    return true;
  }
  
  public void togglePause() {
    mThread.setPaused(!mThread.isPaused());
  }
  
  public void setTextView(TextView tv) {
    mTextView = tv;
  }
}
