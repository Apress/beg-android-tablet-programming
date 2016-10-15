package com.apress.ba3tp.contacts2;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.apress.ba3tp.utils.ListPicker;
import com.apress.ba3tp.utils.MessageBox;

public class LoaderListFragment extends ListFragment implements
    OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

  
  SimpleCursorAdapter mAdapter; // This is the Adapter being used to display the list's data.
  ContactDetails mDetail; // Adapter to display contact details
  String mCurFilter; // If non-null, this is the current filter the user has provided.
  MessageBox mBox;  // Handy dialog routines.

  // General purpose contact columns
  static final String[] dataColumns = {ContactsContract.Data.DISPLAY_NAME,ContactsContract.Data.MIMETYPE,ContactsContract.Data.RAW_CONTACT_ID,
      Data.DATA1,Data.DATA2,Data.DATA3,Data.DATA4,Data.DATA5,Data.DATA6,Data.DATA7,
      Data.DATA8,Data.DATA9,Data.DATA10,Data.DATA11,Data.DATA12,Data.DATA13,Data.DATA14,
      Data.DATA15};

  // List of editiable contact types
  static final String[] editableTypes = {
      CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
      CommonDataKinds.Email.CONTENT_ITEM_TYPE,
      CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
      CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
      CommonDataKinds.Note.CONTENT_ITEM_TYPE };

  private List<PhoneType> mPhoneTypes;
  
  // List of available contact groups
  Map<Integer,String> mGroups = new Hashtable<Integer,String>();
  private long mDetailId; // ID of detail contacts
  private int mEditId;  // Current editing row id
  private Cursor mEditCursor; // Cursor we are currenly editing 
  
  // Loader IDs
  final static int CONTACT_LOADER = 0;
  final static int CONTACT_DETAIL = 1;
  final static int CONTACT_GROUP = 2;

  // Event Handling
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // Give some text to display if there is no data. In a real
    // application this would come from a resource.
    setEmptyText("No contacts");

    // We have a menu item to show in action bar.
    setHasOptionsMenu(true);

    // Create an empty adapter we will use to display the loaded data.
    mAdapter = new SimpleCursorAdapter(getActivity(),
        android.R.layout.simple_list_item_2, null, new String[] {
            Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS }, new int[] {
            android.R.id.text1, android.R.id.text2 }, 0);
    setListAdapter(mAdapter);
    mDetail = new ContactDetails(getActivity(), null, 0);
//    mDetail.add("Details go here.");
    getDetailView().setAdapter(mDetail);
    getDetailView().setOnItemClickListener(this);
    getActivity().registerForContextMenu(getDetailView());
    getLoaderManager().initLoader(CONTACT_LOADER, null, this);
    getLoaderManager().initLoader(CONTACT_GROUP, null, this);
    loadPhoneTypes();
    mBox = new MessageBox(getActivity(),"Contacts");
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Cursor c = mAdapter.getCursor();
    c.moveToPosition(position);
    mDetailId=id;
    getLoaderManager().restartLoader(CONTACT_DETAIL, null, this);
  }

  // Menu
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Place an action bar item for searching.
    MenuItem item = menu.add("Search");
    item.setIcon(android.R.drawable.ic_menu_search);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    SearchView sv = new SearchView(getActivity());
    sv.setOnQueryTextListener(this);
    item.setActionView(sv);
  }

  @Override
  public void onItemClick(AdapterView<?> l, View v, int position, long id) {
    Cursor c = mDetail.getCursor();
    c.moveToPosition(position);
    String type = columnData(c,ContactsContract.Data.MIMETYPE);
    if (isEditable(type)) {
      editRow(c);
    } else {
      toast("Can't edit that type.");
    }
  }

  public boolean detailOption(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      return false;
    }
    final Cursor c = mDetail.getCursor();
    final LoaderListFragment parent = this;
    c.moveToPosition(info.position);
    mEditId = columnDataInt(c, ContactsContract.Data._ID);
    if (item.getTitle().equals("Add Phone")) {
      ContentValues values = new ContentValues();
      long rawid = columnDataLong(c, Phone.RAW_CONTACT_ID);
      values.put(Phone.RAW_CONTACT_ID,rawid);
      values.put(Phone.NUMBER,"0400-999-999");
      values.put(Phone.TYPE, Phone.TYPE_MOBILE);
      values.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
      try {
        ContentResolver cr = getActivity().getContentResolver(); 
        Uri result=cr.insert(ContactsContract.Data.CONTENT_URI, values);
        getLoaderManager().restartLoader(CONTACT_DETAIL, null, this);
        Log.v("Contacts2",result.toString());
        toast(result);
      } catch (Exception e) {
        Log.e("Contacts2","Insert",e);
        toast(e);
      }
      return true;
    } else if (item.getTitle().equals("Type")) {
      ListPicker pick = new ListPicker(getActivity(), mPhoneTypes);
      pick.showDialog(new ListPicker.OnSelectedListener() {
        
        @Override
        public void onSelected(Object o) {
          ContentValues values = new ContentValues();
          values.put(Phone.TYPE,((PhoneType) o).type);
          Uri uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, String.valueOf(mEditId));
          getActivity().getContentResolver().update(uri, values, null, null);
          getLoaderManager().restartLoader(CONTACT_DETAIL, null, parent);
        }
      });
      return true;
    } else if (item.getTitle().equals("Delete")) {
        String s = getDataLine(c);
        mBox.promptMessage("Delete",s,new DialogInterface.OnClickListener() {
          
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Uri uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, String.valueOf(mEditId));
            try {
            parent.getActivity().getContentResolver().delete(uri,null,null);
            } catch (Exception e) {
              Log.e("Contact2","Delete",e);
              toast(e);
            }
          }
        });
    }
    return false;
  }

  // Search Bar
  public boolean onQueryTextChange(String newText) {
    // Called when the action bar search text has changed. Update
    // the search filter, and restart the loader to do a new query
    // with this filter.
    mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
    getLoaderManager().restartLoader(0, null, this);
    return true;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    // Don't care about this.
    return true;
  }

  // Utility functions
  public String columnData(Cursor c, String column) {
    int i = c.getColumnIndex(column);
    if (i<0) return null;
    if (c.getType(i)==Cursor.FIELD_TYPE_BLOB) return "<BLOB>";
    return c.getString(i);
  }

  public int columnDataInt(Cursor c, String column) {
    int i = c.getColumnIndex(column);
    if (i<0) return 0;
    return c.getInt(i);
  }
  
  public long columnDataLong(Cursor c, String column) {
    int i = c.getColumnIndex(column);
    if (i<0) return 0;
    return c.getLong(i);
  }

  private ListView getDetailView() {
    return (ListView) getActivity().findViewById(R.id.listView1);
  }

  public void toast(Object msg) {
    Toast.makeText(getActivity(), msg.toString(), Toast.LENGTH_SHORT).show();
  }

  // Contact Display Routines
  String dumpDataLine(Cursor c) { // Just get the raw data.
    StringBuilder s = new StringBuilder();
    StringBuilderPrinter p = new StringBuilderPrinter(s);
    for (String key : dataColumns) {
      int i = c.getColumnIndex(key);
      if (i>=0 && !c.isNull(i)) {
        p.println(key+" : "+columnData(c, key));
      }
    }
    return s.toString();
  }

  private void loadPhoneTypes() {
    mPhoneTypes = new ArrayList<PhoneType>();
    mPhoneTypes.add(new PhoneType(Phone.TYPE_HOME, "Home"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_MOBILE, "MOBILE"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_WORK, "WORK"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_FAX_WORK, "FAX_WORK"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_FAX_HOME, "FAX_HOME"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_PAGER, "PAGER"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_OTHER, "OTHER"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_CALLBACK, "CALLBACK"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_CAR, "CAR"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_COMPANY_MAIN, "COMPANY_MAIN"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_ISDN, "ISDN"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_MAIN, "MAIN"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_OTHER_FAX, "OTHER_FAX"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_RADIO, "RADIO"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_TELEX, "TELEX"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_TTY_TDD, "TTY_TDD"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_WORK_MOBILE, "WORK_MOBILE"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_WORK_PAGER, "WORK_PAGER"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_ASSISTANT, "ASSISTANT"));
    mPhoneTypes.add(new PhoneType(Phone.TYPE_MMS, "MMS"));
  }

  String getPhoneType(Cursor c) {
    int i = c.getColumnIndex(CommonDataKinds.Phone.TYPE);
    int type = c.getInt(i);
    for (PhoneType p : mPhoneTypes) {
      if (p.type==type) return p.description;
    }
    // Fallthrough: use label.
    return columnData(c, CommonDataKinds.Phone.LABEL);
  }
  
  String getDataLine(Cursor c) { //Translate various contact data types.
    String type = columnData(c,ContactsContract.Data.MIMETYPE);
    if (type.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
      return "Phone="+columnData(c,CommonDataKinds.Phone.NUMBER)+" "+getPhoneType(c);
    } else if (type.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
      return "Email="+columnData(c,CommonDataKinds.Email.ADDRESS);
    } else if (type.equals(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
      return "Name="+columnData(c,CommonDataKinds.StructuredName.DISPLAY_NAME);
    } else if (type.equals(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)) {
      return "Nickname="+columnData(c,CommonDataKinds.Nickname.DISPLAY_NAME);
    } else if (type.equals(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
      return "Address="+columnData(c,CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
    } else if (type.equals(CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
      byte[] b = c.getBlob(c.getColumnIndex(CommonDataKinds.Photo.PHOTO));
      ImageView iv = (ImageView) getActivity().findViewById(R.id.imageView1);
      if (b!=null) {
        iv.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
      } else {
        iv.setImageResource(R.drawable.icon);
      }
      return "Photo="+columnData(c,CommonDataKinds.Photo.PHOTO);
    } else if (type.equals(CommonDataKinds.Note.CONTENT_ITEM_TYPE)) {
      return "Note="+columnData(c,CommonDataKinds.Note.NOTE);
    } else if (type.equals(CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)) {
      return "Group="+mGroups.get(columnDataInt(c,CommonDataKinds.GroupMembership.GROUP_ROW_ID));
    } else if (type.equals("vnd.com.google.cursor.item/contact_misc")) {
      return "Misc";
    } else {
      return dumpDataLine(c);
    }
  }
  
  private boolean isEditable(String type) {
    for (String s : editableTypes) {
      if (s.equals(type)) return true;
    }
    return false;
  }
  
  private void editRow(Cursor c) {
    mEditId = columnDataInt(c, ContactsContract.Data._ID);
    mEditCursor = c;
    Bundle b = new Bundle();
    b.putString("text",columnData(c,ContactsContract.Data.DATA1)); // Usually things are in data1.
    getActivity().showDialog(0,b);  
  }

  public void modifyData(String newValue) {
    if (mEditCursor==null) return;
    ContentValues values = new ContentValues();
    values.put(ContactsContract.Data.DATA1, newValue); // This is a bit quick and dirty
    Uri uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, String.valueOf(mEditId));
    try {
      getActivity().getContentResolver().update(uri, values, null,null);
    } catch (Exception e) {
      toast(e);
    }
    getLoaderManager().restartLoader(CONTACT_DETAIL, null, this);
  }

  // Loader Managerment

  static final String[] CONTACTS_SUMMARY_COLUMNS = new String[] {
      ContactsContract.Data._ID,
      ContactsContract.Data.DISPLAY_NAME, 
      ContactsContract.Data.CONTACT_STATUS};

  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    // This is called when a new Loader needs to be created.
    if (id == CONTACT_LOADER) {
      Uri baseUri;
      if (mCurFilter != null) {
        baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
            Uri.encode(mCurFilter));
      } else {
        baseUri = ContactsContract.Contacts.CONTENT_URI;
      }

      // Now create and return a CursorLoader that will take care of
      // creating a Cursor for the data being displayed.
      String select = "(" + Contacts.DISPLAY_NAME + " is not null)";
      return new CursorLoader(getActivity(), baseUri,
          CONTACTS_SUMMARY_COLUMNS, select, null, Contacts.DISPLAY_NAME
              + " COLLATE LOCALIZED ASC");
    } else if (id == CONTACT_DETAIL) {
      return new CursorLoader(getActivity(),ContactsContract.Data.CONTENT_URI, null,
          ContactsContract.Data.CONTACT_ID + "=" + mDetailId, null, null);
    } else if (id == CONTACT_GROUP) {
      return new CursorLoader(getActivity(),ContactsContract.Groups.CONTENT_URI, null,
          null, null, null);
    } else {
      return null;
    }
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    int id = loader.getId();
    if (id==CONTACT_LOADER) {
      mAdapter.swapCursor(data);
    } else if (id==CONTACT_DETAIL) {
      mDetail.swapCursor(data);
    } else if (id==CONTACT_GROUP) {
      int title = data.getColumnIndex(ContactsContract.Groups.TITLE);
      int group_id = data.getColumnIndex(ContactsContract.Groups._ID);
      mGroups.clear();
      for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
        mGroups.put(data.getInt(group_id),data.getString(title));
      }
    }
  }

  public void onLoaderReset(Loader<Cursor> loader) {
    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed. We need to make sure we are no
    // longer using it.
    if (loader.getId()==CONTACT_LOADER) mAdapter.swapCursor(null);
    else if (loader.getId()==CONTACT_DETAIL) mDetail.swapCursor(null);
  }
  
  // Contact Detail Adapter
  class ContactDetails extends CursorAdapter {

    public ContactDetails(Context context, Cursor c, int flags) {
      super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      TextView tv = (TextView) view.findViewById(android.R.id.text1);
      tv.setText(getDataLine(cursor));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      final LayoutInflater inflater = LayoutInflater.from(context);
      View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
      bindView(v, context, cursor);
      return v;
    }
  }
  
  // Contains PhoneType lookup.
  public class PhoneType {
    int type;
    String description;
    
    PhoneType(int atype, String adescription) {
      type=atype;
      description=adescription;
    }
    
    @Override
    public String toString() {
      return description;
    }
  }

}