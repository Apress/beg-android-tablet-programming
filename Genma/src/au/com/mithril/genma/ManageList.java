package au.com.mithril.genma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.apress.ba3tp.utils.FilePicker;
import com.apress.ba3tp.utils.FilePicker.OnSelectedListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.StringBuilderPrinter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class ManageList extends ListActivity {
  
  static final int DIALOG_EDIT = 1;
  static final int DIALOG_DELETE = 2;
  static final int DIALOG_EXPORT = 3;
  
  ArrayAdapter<String> mAdapter;
  private SharedPreferences mPreferences;
  EditText mInput;
  FilePicker mPicker;
  int mCurrent;
  private EditText mExport;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    List<String> items = loadList(mPreferences);
    if (items == null) {
      items = new ArrayList<String>();
      items.add("Example Text");
    }
    mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
    setListAdapter(mAdapter);
    for (String s:items) mAdapter.add(s);
    // mAdapter.addAll(items); // too recent for phone
    registerForContextMenu(getListView());
  }

  @Override
  protected void onPause() {
    List<String> mylist = new ArrayList<String>(mAdapter.getCount());
    for (int i=0; i<mAdapter.getCount(); i++) mylist.add(mAdapter.getItem(i));
    saveList(mylist,mPreferences);
    super.onPause();
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      return false;
    }
    mCurrent = info.position;
    String value = mAdapter.getItem(mCurrent);
    String menuname = (String) item.getTitle();
    if (menuname.equals("Delete")) {
      showDialog(DIALOG_DELETE);
    } else if (menuname.equals("Edit")) {
      Bundle b = new Bundle();
      b.putString("text", value);
      showDialog(DIALOG_EDIT,b);
    } else if (menuname.equals("Add")) {
      mCurrent=-1;
      showDialog(DIALOG_EDIT);
    }

    return super.onContextItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add("Edit");
    menu.add("Delete");
    menu.add("Add");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MainActivity.addAction(menu.add("Add"));
    menu.add("Export");
    menu.add("Import");
    menu.add("Share").setIcon(android.R.drawable.ic_menu_share);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    String title = (String) item.getTitle();
    if (title.equals("Add")) {
      mCurrent=-1;
      showDialog(DIALOG_EDIT);
    } else if (title.equals("Export")) {
      showDialog(DIALOG_EXPORT);
    } else if (title.equals("Import")) {
      askImport();
    } else if (title.equals("Share")) {
      shareList();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected Dialog onCreateDialog(int id, Bundle args) {
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    b.setTitle("List Manager");
    if (id==DIALOG_EDIT) {
      mInput = new EditText(this);
      b.setView(mInput);
      b.setPositiveButton("OK", new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (mCurrent>=0) {
            String s = mAdapter.getItem(mCurrent);
            mAdapter.remove(s);
            mAdapter.insert(mInput.getText().toString(), mCurrent);
          } else {
            mAdapter.add(mInput.getText().toString());
          }  
        }
      });
      return b.create();
    } else if (id==DIALOG_DELETE) {
      b.setMessage("Delete this item?");
      b.setPositiveButton("Yes", new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (mCurrent>=0) {
            String s = mAdapter.getItem(mCurrent);
            mAdapter.remove(s);
          }  
        }
      });
      b.setNegativeButton("No", null);
      return b.create();
    } else if (id==DIALOG_EXPORT) {
      b.setMessage("Export to");
      File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File dest = new File(downloads,"genma.txt");
      mExport = new EditText(this);
      mExport.setText(dest.toString());
      b.setView(mExport);
      b.setPositiveButton("Export", new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          exportFile(mExport.getText().toString()); 
        }
      });
      return b.create();
    }
    
    return super.onCreateDialog(id, args);
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    if (id==DIALOG_EDIT) {
      String text = null;
      if (args!=null) {
        text = args.getString("text");
      }
      mInput.setText(text);
    }
    super.onPrepareDialog(id, dialog, args);
  }



  public static List<String> loadList(SharedPreferences preferences) {
    List<String> result = null;
    int count = preferences.getInt("list.count", 0);
    if (count > 0) {
      result = new ArrayList<String>(count);
      for (int i = 0; i < count; i++)
        result.add(preferences.getString("list." + i, "empty"));
    }
    return result;
  }
  
  public static void saveList(List<String> list, SharedPreferences preferences) {
    SharedPreferences.Editor e = preferences.edit();
    int count = preferences.getInt("list.count", 0);
    for (int i = 0; i < count; i++) {
      e.remove("list." + i);
    }
    e.putInt("list.count", list.size());
    for (int i=0; i<list.size(); i++) e.putString("list."+i, list.get(i));
    e.commit();
  }
  
  public static void addList(String text, SharedPreferences preferences){
    List<String> mylist = loadList(preferences);
    if (mylist==null) mylist=new ArrayList<String>(1);
    if (mylist.contains(text)) return;
    mylist.add(text);
    saveList(mylist,preferences);
  }

  protected void exportFile(String path) {
    try {
      File dest = new File(path);
      PrintWriter buf = new PrintWriter(new FileWriter(dest));
      for (int i=0; i<mAdapter.getCount(); i++) buf.println(mAdapter.getItem(i));
      buf.close();
    } catch (Exception e) {
      toast(e);
    }
  }

  protected void importFile(File source) {
    try {
      BufferedReader buf = new BufferedReader(new FileReader(source));
      List<String> work = new ArrayList<String>();
      String line;
      while ((line=buf.readLine())!=null) {
        work.add(line);
      }
      buf.close();
      mAdapter.clear();
      for (String s:work) {
        mAdapter.add(s);
      }
    } catch (Exception e) {
      toast(e);
    }
  }

  void askImport() {
    if (mPicker!=null) mPicker.dismiss();
    File dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    mPicker= new FilePicker(this,dest,"Import text file","txt");
    mPicker.showDialog(new OnSelectedListener() {
      
      @Override
      public void onSelected(File file) {
        importFile(file);
      }
    });
  }
  
  void shareList() {
    StringBuilder sb = new StringBuilder();
    StringBuilderPrinter p = new StringBuilderPrinter(sb);
    for (int i=0; i<mAdapter.getCount(); i++) p.println(mAdapter.getItem(i));
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
    intent=Intent.createChooser(intent, "Share List");
    if (intent!=null) {
      startActivity(intent);
    }
  }
  
  private void toast(Object o) {
    Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
  }

  
}
