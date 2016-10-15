package com.apress.ba3tp.explorer;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apress.ba3tp.explorer.FileUtils.FileStatus;

public class FileListAdapter extends BaseAdapter implements Comparator<File> {


  protected final Context mContext;
  protected final LayoutInflater mInflater;
  protected List<File> mFiles;
  private PackageManager mPackageManager;

  public boolean mShowPermissions;
  public boolean mShowOwner;
  public boolean mShowHidden=false;
  public boolean mGroupFolders=true;
  public int mSortBy = 0; // Sort order
  public DateFormat mDateFormat = DateFormat.getDateTimeInstance();
  
  public FileListAdapter(Context context) {
    mContext = context;
    mInflater = (LayoutInflater) mContext
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mPackageManager = mContext.getPackageManager();
    mShowPermissions = true;
  }

  @Override
  public int getCount() {
    return getFileList().size();
  }

  @Override
  public Object getItem(int position) {
    return getFileList().get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  // Build the individual list item.
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout container;
    File file = getFileList().get(position);
    String mime = getMimeFromFile(file);

    if (convertView == null) {
      container = (LinearLayout) mInflater.inflate(R.layout.filelist, null);
    } else {
      container = (LinearLayout) convertView;
    }

    ImageView icon = (ImageView) container.findViewById(R.id.image1);
    int resourceId = 0;
    if (file.toString().equals("..")) {
      resourceId = android.R.drawable.ic_menu_revert;
    } else if (file.isDirectory()) {
      resourceId = R.drawable.folder;
    } else {
      Drawable d = getFileIcon(file);
      if (d != null) {
        icon.setImageDrawable(d);
        resourceId = -1;
      }
    }
    if (resourceId == 0)
      resourceId = R.drawable.file;
    if (resourceId >= 0) {
      icon.setImageResource(resourceId);
    }
    TextView text = (TextView) container.findViewById(R.id.tvFileName);
    TextView extra = (TextView) container.findViewById(R.id.tvAdditional);
    text.setText(file.getName());
    String perms;
    String line = "";
    if (mShowPermissions) {
      try {
        perms = FileUtils.permString(FileUtils.getPermissions(file));
      } catch (Exception e) {
        perms = "????";
      }
      line += " " + perms;
    }
    line += " " + file.length()+" " + mDateFormat.format(new Date(file.lastModified()));
    if (mShowOwner) {
      String owner = "";
      try {
        FileStatus fs = FileUtils.getFileStatus(file);
        if (fs.uid != 0) {
          owner = mPackageManager.getNameForUid(fs.uid);
        }
      } catch (Exception e) {
        owner = "?";
      }
      line += " " + owner;
    }
    if (mime != null)
      line += " " + mime;
    extra.setText(line.trim());
    return container;
  }

  private String getMimeFromFile(File file) {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
        FileUtils.getExtension(file));
  }
  
  private Drawable getIntentIcon(Intent intent) {
    Drawable result = null;
    try {
      result=mPackageManager.getActivityIcon(intent);
    } catch (Exception e) {
      result=null;
    }
    return result;
  }
  
  public Intent getViewIntentForFile(File file) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    String mime = getMimeFromFile(file);
    intent.setDataAndType(Uri.fromFile(file), mime);
    return intent;
  }  
  
  private Drawable getFileIcon(File file) {
    Drawable result = null;
    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.fromFile(file));
    result=getIntentIcon(intent);
    if (result==null) {
      String mime = getMimeFromFile(file);
      if (mime!=null) {
        intent.setDataAndType(Uri.fromFile(file), mime);
        result=getIntentIcon(intent);
        if (result==null) {
          intent.setType(mime);
          result=getIntentIcon(intent);
        }
      }
    }
    return result;
  }

    public List<File> getFileList() {
    return mFiles;
  }

  public void setFileList(List<File> list) {
    mFiles = list;
    notifyDataSetChanged();
  }

  public void setFileList(File file) throws Exception {
    File[] list = file.listFiles();
    if (list==null) {
      throw new Exception("Access denied.");
    }
    ArrayList<File> files = new ArrayList<File>(list.length);
    for (File f : list) {
      if (mShowHidden || !f.isHidden()) {
        files.add(f);
      }  
    }
    Collections.sort(files,this);
    if (file.getParentFile()!=null) files.add(0,new File(".."));
    setFileList(files);
  }

  private String fileSort(File file) {
    String key;
    if (mSortBy==1) key= String.format("%11d",file.length());
    else if (mSortBy==2) key=String.format("%11d", file.lastModified());
    else key=file.getName().toLowerCase();
    if (mGroupFolders) key=(file.isDirectory() ? "1" : "2")+key;
    return key;
  }
  
  @Override
  public int compare(File file1, File file2) {
    return fileSort(file1).compareTo(fileSort(file2));
  }
}
