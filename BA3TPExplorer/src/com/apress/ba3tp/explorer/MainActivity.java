package com.apress.ba3tp.explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements
    OnSharedPreferenceChangeListener {

  // Declarations  
  private static enum MenuId {
    OPEN, PREFERENCES, RENAME, DELETE, COPY, PASTE,NEWFOLDER,NEWFILE;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum DialogId {
    ASKTYPE, RENAME,PROCEED;
    public int getId() {
      return ordinal();
    }
  }

  FileListAdapter mAdapter;
  File mCurrentDir;
  private File mCurrentFile;
  private File mLastGoodDir;
  private SharedPreferences mPreferences;
  private EditText mEditText;
  private EditText mInputText;
  private Dialog mDialog; // Unmanaged Dialog Instance.
  private File mClipboard; // File to copy.
  private File mDestination; // Destination for copy.
  
// Menu Stuff  
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    int id = item.getItemId();
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      return false;
    }
    File file = (File) mAdapter.getItem(info.position);
    if (id == MenuId.RENAME.getId()) {
      mCurrentFile = file;
      showDialog(DialogId.RENAME.getId());
    } else if (id == MenuId.OPEN.getId()) {
      doOpen(file);
    } else if (id == MenuId.DELETE.getId()) {
      doDelete(file);
    } else if (id == MenuId.COPY.getId()) {
      mClipboard=file;
      toast("Copied to clipboard");
    } else if (id == MenuId.PASTE.getId()) {
      doPaste(file);
    }
    return true;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(Menu.NONE, MenuId.OPEN.getId(), Menu.NONE, "Open");
    menu.add(Menu.NONE, MenuId.RENAME.getId(), Menu.NONE, "Rename");
    menu.add(Menu.NONE,MenuId.DELETE.getId(), Menu.NONE, "Delete");
    menu.add(Menu.NONE,MenuId.COPY.getId(), Menu.NONE,"Copy");
    if (mClipboard!=null) {
      menu.add(Menu.NONE,MenuId.PASTE.getId(), Menu.NONE, "Paste");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    
    menu.add(Menu.NONE, MenuId.OPEN.getId(), Menu.NONE, "Open")
        .setIcon(android.R.drawable.ic_menu_view)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences")
        .setIcon(android.R.drawable.ic_menu_preferences)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    SubMenu m = menu.addSubMenu("New")
      .setIcon(android.R.drawable.ic_menu_add);
    m.add(Menu.NONE,MenuId.NEWFILE.getId(),Menu.NONE,"File").
      setIcon(R.drawable.file);
    m.add(Menu.NONE,MenuId.NEWFOLDER.getId(), Menu.NONE,"Folder").
      setIcon(R.drawable.folder);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == MenuId.OPEN.getId()) {
      File file = getSelectedFile();
      doOpen(file);
    } else if (id == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, Preferences.class));
    } else if (id == MenuId.NEWFILE.getId()) {
      inputPrompt("New File", "Enter new file name", "OK", new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          newFile(mInputText.getText().toString(),false);  
        }
      });
  } else if (id == MenuId.NEWFOLDER.getId()) {
    inputPrompt("New Folder", "Enter new folder name", "OK", new OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        newFile(mInputText.getText().toString(),true);  
      }
    });
}
    return super.onOptionsItemSelected(item);
  }


    // Dialog Stuff
  @Override
  protected Dialog onCreateDialog(int id, Bundle args) {
    if (id == DialogId.ASKTYPE.getId()) {
      AlertDialog.Builder b = new AlertDialog.Builder(this);
      String[] list = { "Text", "HTML" };
      b.setItems(list, new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          if (which == 0)
            intent.setDataAndType(Uri.fromFile(mCurrentFile), "text/plain");
          else if (which == 1)
            intent.setDataAndType(Uri.fromFile(mCurrentFile), "text/html");
          try {
            startActivity(intent);
          } catch (Exception e) {
            toast(e);
          }
        }
      });
      return b.create();
    } else if (id == DialogId.RENAME.getId()) {
      AlertDialog.Builder b = new AlertDialog.Builder(this);
      b.setTitle("Rename");
      b.setView(mEditText);
      b.setPositiveButton("Ok", new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          String newname = mEditText.getText().toString();
          File newfile = new File(mCurrentFile.getParentFile(),newname);
          if (mCurrentFile.renameTo(newfile)) {
            setCurrentDir(mCurrentDir); // Reload screen.
          } else {
            toast("Rename to " + newfile + " failed.");
          }
        }
      });
      return b.create();
    }
    return super.onCreateDialog(id, args);
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    if (id == DialogId.RENAME.getId()) {
      mEditText.setText(mCurrentFile.getName());
    }
    super.onPrepareDialog(id, dialog);
  }

  // Tidy up unmanaged dialog.
  public void clearDialog() {
    if (mDialog!=null) {
      mDialog.dismiss();
      mDialog=null;
    }
  }
  
  public void showMessage(Object message) {
    showMessage("BA3TP Explorer",message);
  }
  
  public void showMessage(String caption, Object message) {
    promptMessage(caption,message,"Ok",new DialogInterface.OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    }, caption, null);
    
  }
  
  public void promptMessage(String caption, Object message, DialogInterface.OnClickListener onPositive) {
    promptMessage(caption,message,"Yes",onPositive,"No",new DialogInterface.OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
        
  }

  public void promptMessage(String caption, Object message, String positive, DialogInterface.OnClickListener onPositive, String negative, DialogInterface.OnClickListener onNegative) {
    clearDialog();
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    b.setTitle(caption);
    b.setMessage(message.toString());
    if (positive==null) positive="Yes";
    if (onPositive!=null) b.setPositiveButton(positive, onPositive);
    if (negative==null) negative="No";
    if (onNegative!=null) b.setNegativeButton(negative, onNegative);
    mDialog=b.create();
    mDialog.show();
  }

  public void inputPrompt(String caption, Object message, String positive, DialogInterface.OnClickListener onPositive) {
    clearDialog();
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    b.setTitle(caption);
    b.setMessage(message.toString());
    mInputText=new EditText(this);
    b.setView(mInputText);
    b.setPositiveButton("OK", onPositive);
    mDialog=b.create();
    mDialog.show();
  }
// Event Handling  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAdapter = new FileListAdapter(this);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mPreferences.registerOnSharedPreferenceChangeListener(this);

    String startDir = "/sdcard";
    if (savedInstanceState != null
        && savedInstanceState.containsKey("startDir")) {
      startDir = savedInstanceState.getString("startDir");
    }
    setCurrentDir(new File(startDir));
    getListView().setAdapter(mAdapter);
    loadPreferences();
    registerForContextMenu(getListView()); // Allow context menu to appear
    mEditText = new EditText(this);
  }


  @Override
  protected void onStart() {
    super.onStart();
    ActionBar a = getActionBar(); // Actionbar doesn't seem to be available in
                                  // onCreate.
    if (a != null)
      a.setSubtitle(mCurrentDir.getAbsolutePath());
  }

  @Override
  protected void onDestroy() {
    clearDialog(); // Don't know if this is essential, but may as well be neat.
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (mCurrentDir != null && mCurrentDir.getParent() != null) {
      setCurrentDir(mCurrentDir.getParentFile());
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString("startDir", mCurrentDir.toString());
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    File f = mAdapter.getFileList().get(position);
    mCurrentFile = null;
    if (f.toString().equals("..")) {
      setCurrentDir(mCurrentDir.getParentFile());
    } else if (f.isDirectory()) {
      setCurrentDir(f);
    } else {
      mCurrentFile = f;
    }
    super.onListItemClick(l, v, position, id);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
    loadPreferences();
    doRefresh(); // Reload screen.
  }

  
// Utility functions  
  private void setCurrentDir(File file) {
    try {
      mCurrentDir = file;
      mAdapter.setFileList(file);
      mLastGoodDir = file;
    } catch (Exception e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
      mCurrentDir = mLastGoodDir;
      try {
        mAdapter.setFileList(mLastGoodDir);
      } catch (Exception ex) {
        finish();
      }
    }
    ActionBar a = getActionBar();
    if (a != null)
      a.setSubtitle(mCurrentDir.getAbsolutePath().toString());
  }

  void toast(Object msg) {
    Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show();
  }


  private void doOpen(File file) {
    if (file != null && !file.isDirectory()) {
      try {
        Intent intent = mAdapter.getViewIntentForFile(file);
        startActivity(intent);
      } catch (ActivityNotFoundException a) {
        mCurrentFile=file;
        askOpen(); // Don't know how to handle this. Ask.
      } catch (Exception e) {
        toast(e);
      }
    }
  }

  private void askOpen() {
    showDialog(DialogId.ASKTYPE.getId());
  }

  private void doDelete(File file) {
    final File delFile=file;
    promptMessage("Delete",file,new DialogInterface.OnClickListener() {
      
      @Override
      public void onClick(DialogInterface dialog, int which) {
        
        if (delFile.delete()) {
          toast("File deleted.");
          doRefresh();
        } else {
          toast("Delete failed.");
        }
      }
    });
  }

  private void doPaste(File file) {
    File dest;
    if (mClipboard==null) return;
    if (file.isDirectory()) {
      dest=new File(file,mClipboard.getName());
    } else {
      dest = file;
    }
    mDestination = dest;
    if (dest.exists()) {
      promptMessage("Copy from "+mClipboard,"to "+mDestination, new OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          performCopy();
        }
      });      
    }
    else {
      performCopy();
    }
  }

  protected void performCopy() {
    if (performCopy(mClipboard,mDestination)) {
      mClipboard=null;
      mDestination=null;
    }  
    doRefresh();
  }

  private boolean performCopy(File source, File dest) {
    if (source.equals(dest))
      return false;
    byte[] buffer = new byte[4096];
    try {
      File to;
      if (source.isDirectory()) {
        for (File from : source.listFiles()) {
          if (dest.isDirectory()) {
            to=new File(dest,from.getName()); 
          } else to=new File(dest.getParent(),from.getName());
          performCopy(from,to);
        }
      }
      else {
        if (dest.getParentFile()!=null && !dest.getParentFile().exists()) {
          dest.getParentFile().mkdirs(); // Make sure dest path exists.
        }
        FileInputStream input = new FileInputStream(source);
        FileOutputStream output = new FileOutputStream(dest);
        try {
        int bytes;
        while ((bytes=input.read(buffer))>0) {
          output.write(buffer,0,bytes);
        }
        } finally {
          input.close();
          output.close();
        }  
      }
      return true;
    } catch (Exception e) {
      showMessage("Copy", e);
      return false;
    }
  }

  protected void doRefresh() {
    setCurrentDir(mCurrentDir);
  }

  private void loadPreferences() {
    mAdapter.mShowHidden = mPreferences.getBoolean("showhidden", false);
    mAdapter.mGroupFolders = mPreferences.getBoolean("groupfolders", true);
    mAdapter.mShowOwner = mPreferences.getBoolean("showowner", false);
    mAdapter.mShowPermissions = mPreferences.getBoolean("showperms", false);
    Resources res = getResources();
    String[] sorts = res.getStringArray(R.array.sortOrder);
    List<String> list = Arrays.asList(sorts);
    String sortby = mPreferences.getString("sortorder", "Name");
    mAdapter.mSortBy = list.indexOf(sortby);
  }

  public File getSelectedFile() {
    return mCurrentFile;
  }

  protected void newFile(String string, boolean isFolder) {
    if (string==null || string.equals("")) {
      toast("No name entered");
      return;
    }
    File file = new File(mCurrentDir, string);
    if (isFolder) {
      if (!file.mkdir()) toast("mkdir failed");
    } else {
      try {
        file.createNewFile();
      } catch (IOException e) {
        toast(e);
      }
    }
    doRefresh();
  }


}
