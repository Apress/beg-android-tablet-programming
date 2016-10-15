package com.apress.ba3tp.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GameSurfaceGl extends GLSurfaceView implements Renderer {
	
	/** Cube instance */
	private Cube cube;	
	
	/* Rotation values */
	private float xrot;					//X Rotation
	private float yrot;					//Y Rotation

	private float z = -5.0f;			//Depth Into The Screen
	private float SCALE = 0.2f; // Something that looks nice when rotating.
	
	private boolean light = false;

	/* 
	 * The initial light values for ambient and diffuse
	 * as well as the light position ( NEW ) 
	 */
	private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};
		
	/* The buffers for our light values ( NEW ) */
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightPositionBuffer;
	
	private float oldX;
  private float oldY;
	private long lastTime = System.currentTimeMillis();
	private int frames = 0;
	
	/** The Activity Context */
	private Context context;

  private Handler mHandler;
	
	public GameSurfaceGl(Context context, AttributeSet attrs) {
	  super(context,attrs);
	  init(context);
	}
	
	public GameSurfaceGl(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		this.setRenderer(this);
		this.requestFocus();
		this.setFocusableInTouchMode(true);
		
		this.context = context;		
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightAmbientBuffer = byteBuf.asFloatBuffer();
		lightAmbientBuffer.put(lightAmbient);
		lightAmbientBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightDiffuseBuffer = byteBuf.asFloatBuffer();
		lightDiffuseBuffer.put(lightDiffuse);
		lightDiffuseBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightPositionBuffer = byteBuf.asFloatBuffer();
		lightPositionBuffer.put(lightPosition);
		lightPositionBuffer.position(0);
		
		cube = new Cube();
	}
	
  public synchronized void setHandler(Handler handler) {
    mHandler=handler;
  }

  private void sendText(String text) {
    if (mHandler!=null) {
      Message msg = mHandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("text",text);
      msg.setData(b);
      mHandler.sendMessage(msg);
    }
  }

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    // Set up various lights.
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);
    // Position the light, and enable it.
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);
    gl.glEnable(GL10.GL_LIGHT0); // Enable Light 0

    // Settings
    gl.glDisable(GL10.GL_DITHER); // Disable dithering
    gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping
    gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
    gl.glClearDepthf(1.0f); // Depth Buffer Setup
    gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
    gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

    // Uss nicest possible Perspective Calculations
    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

    // This is our opportunity to load textures and stuff.
    cube.loadGLTexture(gl, this.context);
  }

  public void onDrawFrame(GL10 gl) {
    // Clear Screen And Depth Buffer
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    gl.glLoadIdentity(); // Reset The Current Modelview Matrix

    // Check if the light flag has been set to enable/disable lighting
    if (light) {
      gl.glEnable(GL10.GL_LIGHTING);
    } else {
      gl.glDisable(GL10.GL_LIGHTING);
    }

    // Drawing
    gl.glTranslatef(0.0f, 0.0f, z); // Move z units into the screen

    // Rotate around the axis based on the rotation matrix (rotation, x, y, z)
    gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f); // X
    gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f); // Y

    cube.draw(gl); // Draw the Cube

    frames += 1;
    long now = System.currentTimeMillis();
    if (now - lastTime >= 1000) {
      String s = "xrot=" + xrot + ",yrot=" + yrot + " depth=" + z + " frames="
          + frames;
      sendText(s);
      frames = 0;
      lastTime = now;
    }
  }

  public void onSurfaceChanged(GL10 gl, int width, int height) {
    if (height <= 0) height = 1; // Avoid divide by zero.
    gl.glViewport(0, 0, width, height); // Reset The Current Viewport
    gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
    gl.glLoadIdentity(); // Reset The Projection Matrix

    // Calculate The Aspect Ratio Of The Window
    GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);

    gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
    gl.glLoadIdentity(); // Reset The Modelview Matrix
  }
	
	/**
	 * Override the touch screen listener.
	 * 
	 * React to moves and presses on the touchscreen.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//
    float x = event.getX();
    float y = event.getY();
    int action = event.getAction();

    // If a touch is moved on the screen
    if (action == MotionEvent.ACTION_MOVE) {
      // Calculate the change
      float dx = x - oldX;
      float dy = y - oldY;
      xrot += dy * SCALE;
      yrot += dx * SCALE;
    } else if (action==MotionEvent.ACTION_UP) {
      queueEvent(new Runnable() {
        
        @Override
        public void run() { // This will happen inside the render thread
          sendText("Up"); // Fairly pointless example... 
        }
      });
    }
    oldX = x;
    oldY = y;
    return true;
	}

  public synchronized void setZoom(int progress) {
    z=-progress;
  }

  public synchronized void setLights(boolean checked) {
    light = checked;
  }
}
