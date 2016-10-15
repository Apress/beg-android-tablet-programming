package com.apress.ba3tp.contacts;

import java.util.ArrayList;
import java.util.List;

import com.apress.ba3tp.contacts.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
  TextView mDisplay;
  Boolean mBack;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mDisplay = (TextView) findViewById(R.id.eDisplay);
  }

  public void onButtonClick(View v) {
    try {
      ContentResolver cr = getContentResolver();
      Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
          null, null);
      display("Contacts");
      if (cur.getCount() > 0) {
        while (cur.moveToNext()) {
          String id = cur.getString(cur
              .getColumnIndex(ContactsContract.Contacts._ID));
          String name = cur.getString(cur
              .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
          addln(id + " : " + name);
        }
      }
      cur.close();
      addln("Done");
    } catch (Exception e) {
      display(e);
    }
  }
  
  String getCurString(Cursor cur,String name) {
    return cur.getString(cur.getColumnIndex(name));
  }
  
  public void onListClick(View v) {
    List<String> mylist = new ArrayList<String>();
    try {
      ContentResolver cr = getContentResolver();
      String[] fields = {Contacts._ID,Contacts.DISPLAY_NAME};
      // This time I'm telling it which fields to display, and in what order.
      Cursor cur = cr.query(Contacts.CONTENT_URI,fields, null, null, Contacts.DISPLAY_NAME);
      if (cur.getCount() > 0) {
        while (cur.moveToNext()) {
          mylist.add(getCurString(cur,Contacts._ID)+" : "+getCurString(cur,Contacts.DISPLAY_NAME));
        }
      }
      cur.close();
      showList(mylist);
    } catch (Exception e) {
      display(e);
    }
  }
  
  public void showList(List<String> mylist) {
    setContentView(R.layout.mylistview);
    ArrayAdapter<String> a = new ArrayAdapter<String>(this,R.layout.mytextview,mylist);
    ListView v = (ListView) findViewById(R.id.listview1);
    v.setAdapter(a);
    mBack=true;
  }  
  
  List<String> buildContactList(Cursor cur) {
    List<String> mylist = new ArrayList<String>();
    String[] names = cur.getColumnNames();
    String data="";
    // Get column names
    for(int i=0; i<cur.getColumnCount(); i++) {
      String s = names[i];
      if (!data.equals("")) data+=":";
      data+=s;
    }
    mylist.add(data);
    // Dump all the columns in each row.
    while (cur.moveToNext()) {
      data="";
      for(int i=0; i<cur.getColumnCount();i++) {
        String s = cur.getString(i);
        if (!data.equals("")) data+=":";
        data+=s;
      }
      mylist.add(data);
    }
    return mylist;
  }
  
  void showQuery(Cursor cur) {
    showList(buildContactList(cur));
  }
  
  // Show only those that have phones
  public void onHasPhoneClick(View v) {
    ContentResolver cr = getContentResolver();
    String[] fields = {Contacts._ID,Contacts.DISPLAY_NAME};
    Cursor cur = cr.query(Contacts.CONTENT_URI,fields, Contacts.HAS_PHONE_NUMBER+"=1",null,Contacts.DISPLAY_NAME);
    showQuery(cur);
  }
  
  // Show names and primary phone numbers
  public void onPhoneClick(View v) {
    ContentResolver cr = getContentResolver();
    String[] fields = {Phone.DISPLAY_NAME, Phone.NUMBER};
    Cursor cur = cr.query(Phone.CONTENT_URI, fields, Phone.IS_PRIMARY+">0",null,Phone.DISPLAY_NAME);
    showQuery(cur);
  }
  
  @Override
  public void onBackPressed() {
    if (mBack) {
      setContentView(R.layout.main);
      mBack=false;
      mDisplay=(TextView) findViewById(R.id.eDisplay);
    }
    else {
      super.onBackPressed();
    }
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
}
