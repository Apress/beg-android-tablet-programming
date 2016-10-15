package au.com.mithril.genma;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

  TextView mText;
  EditText mInput;
  List<String> mList;
  private SharedPreferences mPreferences;
  private AlertDialog mListDialog;
  static final int DIALOG_INPUT = 1;
  static final int DIALOG_SHOW = 2;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mText = (TextView) findViewById(R.id.textView1);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mText.setText(mPreferences.getString("lastmessage", "Genma!!"));
    if (savedInstanceState != null) {
      mText.setText(savedInstanceState.getString("text"));
    }
  }
  
  @Override
  protected void onPause() {
    SharedPreferences.Editor e = mPreferences.edit();
    e.putString("lastmessage", mText.getText().toString());
    e.commit();
    super.onPause();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    if (id == DIALOG_INPUT) {
      AlertDialog.Builder b = new AlertDialog.Builder(this);
      b.setTitle("Enter Sign");
      mInput = new EditText(this);
      mInput.setText(mText.getText().toString());
      b.setView(mInput);
      b.setPositiveButton("OK", new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          setMessageText(mInput.getText().toString());
        }
      });
      return b.create();
    } else if (id == DIALOG_SHOW) {
      AlertDialog.Builder b = new AlertDialog.Builder(this);
      b.setTitle("Info");
      b.setMessage("Message");
      b.setPositiveButton("Ok", null);
      return b.create();
    }
    return super.onCreateDialog(id);
  }

  protected void setMessageText(String msg) {
    mText.setText(msg);
    sizeText();
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    if (id == DIALOG_SHOW) {
      AlertDialog a = (AlertDialog) dialog;
      a.setMessage(args.getString("prompt"));
    } else {
      super.onPrepareDialog(id, dialog, args);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString("text", mText.getText().toString());
    super.onSaveInstanceState(outState);
  }

  void getBounds(TextPaint tp, String text, int maxwidth, Rect r) {
    float width = 0;
    int height = 0;
    int pos = 0;
    int len = text.length();
    float[] measured = new float[1];
    int lineHeight = (int) (tp.getTextSize()+ tp.getFontSpacing());
    while (pos < len) {
      int w = tp.breakText(text, pos, len, true, maxwidth, measured);
      width = Math.max(r.width(), measured[0]);
      height += lineHeight;
      pos += w;
    }
    r.set(0, 0, (int) (width), (int) (height));
  }

  protected void sizeText() {
    View root = findViewById(R.id.root);
    if (root != null) {
      int width = root.getWidth();
      int height = root.getHeight();
      mText.setTextSize(height);
      int size = 16;
      if (width<=0 || height<=0) return;
      mText.setTextSize(size);
      String s = (String) mText.getText();
      Rect r = new Rect();
      TextPaint tp = mText.getPaint();
      while (true) {
        // tp.getTextBounds(s,0,s.length(),r);
        getBounds(tp, s, width, r);
        if (r.width() > width || r.height() > height)
          break;
        size += 1;
        mText.setTextSize(size);
      }
      mText.setTextSize(size - 1);
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    if (hasFocus) sizeText();
    super.onWindowFocusChanged(hasFocus);
  }

  private void toast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    addAction(menu.add("Edit"));
    addAction(menu.add("Info"));
    addAction(menu.add("List"));
    menu.add("Edit List");
    menu.add("Add to List");
    return super.onCreateOptionsMenu(menu);
  }

// This should work on phones AND tablets
  public static void addAction(MenuItem add) {
    if (Build.VERSION.SDK_INT>=11) {
      add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
  }

  public static void addAction2(MenuItem add) {
    try {
      add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    } catch (NoSuchMethodError ne) {
      // Just catch the error silently.
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    String title = (String) item.getTitle();
    if (title.equals("Edit")) {
      showDialog(DIALOG_INPUT);
    } else if (title.equals("Info")) {
      showTextInfo();
    } else if (title.equals("List")) {
      askList();
    } else if (title.equals("Edit List")) {
      startActivity(new Intent(this,ManageList.class));
    } else if (title.equals("Add to List")) {
      ManageList.addList(mText.getText().toString(), mPreferences);
      toast("Added to List");
    }
    return super.onOptionsItemSelected(item);
  }

  private void showTextInfo() {
    TextPaint tp = mText.getPaint();
    FontMetrics fm = tp.getFontMetrics();
    DisplayMetrics dm = new DisplayMetrics();
    Rect r = new Rect();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    String text = mText.getText().toString();
    tp.getTextBounds(text, 0, text.length(), r);
    String msg = "Metrics: " + "\nAscent: " + fm.ascent + "\nBottom: "
        + fm.bottom + "\nDescent: " + fm.descent + "\nLeading: " + fm.leading
        + "\nTop: " + fm.top 
        + "\nSize: " + tp.getTextSize()
        + "\nLeading: " + tp.getFontMetricsInt(null)
        + "\nSpacing: " + tp.getFontSpacing()
        + "\nBounds: " + r
        + "\nMetrics: " + dm;
    showMessage(msg);
  }

  private void showMessage(String msg) {
    Bundle b = new Bundle();
    b.putString("prompt", msg);
    showDialog(DIALOG_SHOW, b);
  }

  private void askList() {
    clearListDialog();
    mList = ManageList.loadList(mPreferences);
    if (mList==null) {
      showMessage("Nothing in List");
      return;
    }
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    b.setTitle("Select from List");
    CharSequence[] array = (CharSequence[]) mList.toArray(new CharSequence[mList.size()]);
    b.setItems(array, new OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (which>=0 && which<mList.size()) {
          setMessageText(mList.get(which));          
        }
      }
    });
    b.setPositiveButton("Ok", null);
    mListDialog = b.create();
    mListDialog.show();
  }
  
  private void clearListDialog() {
    if (mListDialog!=null) {
      mListDialog.dismiss();
      mListDialog=null;
    }
  }
}