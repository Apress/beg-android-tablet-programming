package com.apress.ba3tp.browser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  TextView mDisplay;
  WebView mWebView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mDisplay = (TextView) findViewById(R.id.eDisplay);
  }

  public void onButtonClick(View v) {
    Intent intent = new Intent(Intent.ACTION_VIEW,
        Uri.parse("http://www.google.com"));
    startActivity(intent);
  }

  WebView getWebView() {
    if (mWebView == null)
      mWebView = new WebView(this);
    return mWebView;
  }

  public void onWebViewClick(View v) {
    mWebView = getWebView();
    setContentView(mWebView);
    mWebView.loadUrl("http://www.google.com");
  }

  public void onHtmlClick(View v) {
    mWebView = getWebView();
    setContentView(mWebView);
    mWebView.loadData(
        "<html><head><title>Test</title></head><body>Hi there!</body>",
        "text/html", "utf-8");
  }

  boolean loadFromAsset(String assetName) {
    AssetManager am = getAssets();
    try {
      InputStream is = am.open("test.html");
      try {
        BufferedReader ir = new BufferedReader(new InputStreamReader(is));
        StringBuilder b = new StringBuilder();
        String s;
        while ((s = ir.readLine()) != null) {
          b.append(s + "\n");
        }
        getWebView();
        mWebView.loadData(b.toString(), "text/html", "utf-8");
      } finally {
        is.close();
      }
      return true;
    } catch (Exception e) {
      display(e);
      return false;
    }
  }

  public void onAssetClick(View v) {
    if (loadFromAsset("test.html")) {
      setContentView(mWebView);
    }
  }
  
  public void onFancyClick(View v) {
    getWebView(); // Make sure mWebView is initialised.
    mWebView.getSettings().setJavaScriptEnabled(true); // Allow javascript to run
    mWebView.addJavascriptInterface(new AndroidTest(this), "androidTest");
    mWebView.setWebViewClient(new MyClient());
    if (loadFromAsset("test.html")) {
      setContentView(mWebView);
    }
  }

  @Override
  public void onBackPressed() {
    if (mWebView != null) {
      setContentView(R.layout.main);
      mWebView = null;
    } else
      super.onBackPressed();
  }

  // Show a message on the screen
  public void display(Object msg) {
    mDisplay.setText(msg.toString());
  }

  // Add a line to the existing message
  public void addln(Object msg) {
    String s = (String) mDisplay.getText();
    if (s.equals(""))
      s = msg.toString();
    else
      s += "\n" + msg.toString();
    mDisplay.setText(s);
  }
  
  // This is an example class that can be accessed from within javascript on our web page.
  class AndroidTest {
    final public Context mContext;
    AndroidTest(Context context) {
      mContext=context;
    }
    
    public void showToast(String message) {
      Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
      mWebView.loadUrl("javascript:testme('"+message+"')"); // Just here to show other ways to access page.
    }
  }
  
  // And this is an example class that can be used to change behaviour of your webview
  class MyClient extends WebViewClient {

    @Override
    public void onPageFinished(WebView view, String url) {
      Log.v("ba3tp","Page loaded."); //Writing your own entry into LogCat
      // This is how you can run javascript on your page.
      view.loadUrl("javascript:document.getElementById('myId').innerHTML='Loaded.';");
      super.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return true; // This will force the webview to load all urls internally rather than flicking them to the browser.
    }
    
  }
}