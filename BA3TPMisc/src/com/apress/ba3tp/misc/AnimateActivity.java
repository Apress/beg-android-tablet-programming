package com.apress.ba3tp.misc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class AnimateActivity extends Activity implements AnimationListener {
  View mImage;
  Animation mSpinzoom;
  Animation mFadeAway;
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.animate);
    mImage = findViewById(R.id.imageView1);
    mSpinzoom = AnimationUtils.loadAnimation(this, R.anim.spinscale);
    mFadeAway = AnimationUtils.loadAnimation(this, R.anim.fadeaway);  
    mSpinzoom.setAnimationListener(this);
    mFadeAway.setAnimationListener(this);
  }
  
  void reset() { // Reset our image back to normal.
    mImage.setScaleX(1);
    mImage.setScaleY(1);
    mImage.setVisibility(View.VISIBLE);
  }
  
  public void clickAnimate(View v) {
    reset();
    mImage.startAnimation(mSpinzoom);
  }

  @Override
  public void onAnimationEnd(Animation animation) {
    if (animation==mSpinzoom) {
      mImage.setScaleX(10); // Maintain size we ended up...
      mImage.setScaleY(10); 
      mImage.startAnimation(mFadeAway);
    } else if (animation==mFadeAway) {
      mImage.setVisibility(View.GONE); // And make it go away.
    }
  }

  @Override
  public void onAnimationRepeat(Animation animation) {
  }

  @Override
  public void onAnimationStart(Animation animation) {
  }

}
