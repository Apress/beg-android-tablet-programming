package com.apress.ba3tp.gps;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
  
  ArrayAdapter<Target> mAdapter;
  private SharedPreferences mPreferences;
  View mEditTarget;
  int mCurrent;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    List<Target> items = loadList(mPreferences);
    if (items == null) {
      items = new ArrayList<Target>();
    }
    mAdapter = new ArrayAdapter<Target>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
    setListAdapter(mAdapter);
    for (Target s:items) mAdapter.add(s);
    // mAdapter.addAll(items); // too recent for phone
    registerForContextMenu(getListView());
  }

  @Override
  protected void onPause() {
    List<Target> mylist = new ArrayList<Target>(mAdapter.getCount());
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
    Target value = mAdapter.getItem(mCurrent);
    String menuname = (String) item.getTitle();
    if (menuname.equals("Delete")) {
      showDialog(DIALOG_DELETE);
    } else if (menuname.equals("Edit")) {
      Bundle b = new Bundle();
      b.putString("target", value.getAsString());
      showDialog(DIALOG_EDIT,b);
    } else if (menuname.equals("Add")) {
      mCurrent=-1;
      showDialog(DIALOG_EDIT);
    } else if (menuname.equals("Select")) {
      SharedPreferences.Editor e = mPreferences.edit();
      e.putString("target", value.getAsString());
      e.commit();
      toast("new target set\n"+value.getTitle());
      finish();
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
    menu.add("Select");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add("Add");
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    String title = (String) item.getTitle();
    if (title.equals("Add")) {
      mCurrent=-1;
      showDialog(DIALOG_EDIT);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected Dialog onCreateDialog(int id, Bundle args) {
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    b.setTitle("List Manager");
    if (id==DIALOG_EDIT) {
      mEditTarget=getLayoutInflater().inflate(R.layout.edittarget, null);
      b.setView(mEditTarget);
      b.setPositiveButton("OK", new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Target newTarget = new Target(getEditString());
          if (mCurrent>=0) {
            Target oldTarget = mAdapter.getItem(mCurrent);
            mAdapter.remove(oldTarget);
            mAdapter.insert(newTarget, mCurrent);
          } else {
            mAdapter.add(newTarget);
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
           Target s = mAdapter.getItem(mCurrent);
            mAdapter.remove(s);
          }  
        }
      });
      b.setNegativeButton("No", null);
      return b.create();
    }
    return super.onCreateDialog(id, args);
  }

  protected String getEditString() {
    if (mEditTarget!=null) {
      EditText e1 = (EditText) mEditTarget.findViewById(R.id.eTgtTitle);
      EditText e2 = (EditText) mEditTarget.findViewById(R.id.eTgtLatitude);
      EditText e3 = (EditText) mEditTarget.findViewById(R.id.eTgtLongitude);
      return e2.getText().toString().replace(",",";")+","+
      e3.getText().toString().replace(",",";")+","+
      e1.getText().toString().replace(",",";");
    }
    return null;
  }

  protected void setEditTarget(Target value) {
    if (mEditTarget!=null) {
      EditText e1 = (EditText) mEditTarget.findViewById(R.id.eTgtTitle);
      EditText e2 = (EditText) mEditTarget.findViewById(R.id.eTgtLatitude);
      EditText e3 = (EditText) mEditTarget.findViewById(R.id.eTgtLongitude);
      e1.setText(value.getTitle());
      e2.setText(String.valueOf(value.latitude));
      e3.setText(String.valueOf(value.longitude));
    }
  }
  
  @Override
  protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    if (id==DIALOG_EDIT) {
      if (args!=null) {
        try {
          Target tgt = new Target(args.getString("target"));
          if (tgt!=null) {
            setEditTarget(tgt);
          }
        } catch (Exception e) {
          
        }
      }
    }
    super.onPrepareDialog(id, dialog, args);
  }

  public static List<Target> loadList(SharedPreferences preferences) {
    List<Target> result = null;
    int count = preferences.getInt("targetlist.count", 0);
    if (count > 0) {
      result = new ArrayList<Target>(count);
      for (int i = 0; i < count; i++)
        result.add(new Target(preferences.getString("targetlist." + i, "0,0,empty")));
    }
    return result;
  }
  
  public static void saveList(List<Target> list, SharedPreferences preferences) {
    SharedPreferences.Editor e = preferences.edit();
    int count = preferences.getInt("targetlist.count", 0);
    for (int i = 0; i < count; i++) {
      e.remove("targetlist." + i);
    }
    e.putInt("targetlist.count", list.size());
    for (int i=0; i<list.size(); i++) e.putString("targetlist."+i, list.get(i).getAsString());
    e.commit();
  }
  
  public static void addList(Target target, SharedPreferences preferences){
    List<Target> mylist = loadList(preferences);
    if (mylist==null) mylist=new ArrayList<Target>(1);
    if (mylist.contains(target)) return;
    mylist.add(target);
    saveList(mylist,preferences);
  }

    public void toast(Object o) {
    Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
  }

  
}
