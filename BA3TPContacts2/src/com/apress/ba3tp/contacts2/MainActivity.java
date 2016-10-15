package com.apress.ba3tp.contacts2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
  EditText mEditText;
  ListView mDetailList;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mEditText = new EditText(this);
    mDetailList = (ListView) findViewById(R.id.listView1);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    LoaderListFragment l = (LoaderListFragment) getFragmentManager().findFragmentById(R.id.listLoader);
    return l.detailOption(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    if (v==mDetailList) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
      LoaderListFragment l = (LoaderListFragment) getFragmentManager().findFragmentById(R.id.listLoader);
      Cursor c = l.mDetail.getCursor();
      c.moveToPosition(info.position);
      String type =  l.columnData(c, ContactsContract.Data.MIMETYPE);
      if (type.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
        menu.add("Type");
      }
      menu.add("Add Phone");
      menu.add("Delete");
    }
    super.onCreateContextMenu(menu, v, menuInfo);
  }


  @Override
  protected Dialog onCreateDialog(int id, Bundle args) {
    if (id==0) {
      AlertDialog.Builder b = new AlertDialog.Builder(this);
      b.setTitle("Edit");
      b.setView(mEditText);
      b.setPositiveButton("OK", new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          LoaderListFragment l = (LoaderListFragment) getFragmentManager().findFragmentById(R.id.listLoader);
          l.modifyData(mEditText.getText().toString());
        }
      });
      return b.create();
    }
    return super.onCreateDialog(id, args);
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    if (id == 0) {
       if (args!=null) {
         mEditText.setText(args.getString("text"));
       }
    }
    super.onPrepareDialog(id, dialog, args);
  }

}