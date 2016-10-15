package com.apress.ba3tp.remindme;

import java.util.Date;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class AlarmList extends ListActivity {
  SQLiteDatabase mDb;
  CursorAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDb = (new AlarmDatabase(this)).getWritableDatabase();
    setContentView(R.layout.list); // All this to get empty data to show.
    registerForContextMenu(getListView());
    mAdapter = new CursorAdapter(this,null) {
      @Override
      public View newView(Context context, Cursor cursor, ViewGroup parent) {
        TextView result = new TextView(context);
        result.setTextSize(16);
        bindView(result,context,cursor);
        return result;
      }
      
      @Override
      public void bindView(View view, Context context, Cursor cursor) {
        
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<cursor.getColumnCount(); i++) {
          sb.append(cursor.getColumnName(i)+" : "+cursor.getString(i)+"\n");
        }
        ((TextView) view).setText(sb.toString());
      }
    };
    setListAdapter(mAdapter);
    requery();
  }

  @Override
  protected void onDestroy() {
    mDb.close();
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.listoptions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
    case R.id.menuPurge:
      try {
        mDb.delete("alarms", "trigger<'"+MainActivity.mSqlFmt.format(new Date())+"'", null);
      } catch (SQLiteException e) {
        toast(e);
      }
      requery();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    getMenuInflater().inflate(R.menu.listmenu, menu);
    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      return false;
    }
    int id = item.getItemId();
    switch(id) {
    case R.id.menuDelete:
      clearAlarm((int) info.id);
      mDb.delete("alarms", "_id="+info.id, null);
      requery();
      break;
    }
    return super.onContextItemSelected(item);
  }
  
  void clearAlarm(int id) {
    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    Intent intent = new Intent(this,WakeupHandler.class);
    intent.setData(Uri.withAppendedPath(MainActivity.REMIND_ME_URI, String.valueOf(id)));
    PendingIntent operation = PendingIntent.getBroadcast(this, MainActivity.PENDING_NOTIFY, intent, PendingIntent.FLAG_NO_CREATE);
    if (operation==null) toast("Alarm intent not found");
    else {
      // Should match on Intent.
      am.cancel(operation);
      toast("Alarm cancelled.");
    }
  }
  private void requery() {
    Cursor c = mDb.rawQuery("select * from alarms order by trigger", null);
    mAdapter.changeCursor(c);
  }
  
  private void toast(Object msg) {
    Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show();
  }
}
