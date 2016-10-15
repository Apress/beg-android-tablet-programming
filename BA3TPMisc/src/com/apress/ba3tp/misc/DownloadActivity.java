package com.apress.ba3tp.misc;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadActivity extends Activity {
  EditText mUrl;
  TextView mInfo;
  ListView mList;
  CheckBox mAutoOpen;
  
  SimpleCursorAdapter mAdapter;
  
  DownloadManager dm;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.download);
    dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    mUrl = (EditText) findViewById(R.id.eURL);
    mInfo = (TextView) findViewById(R.id.eInfo);
    mList = (ListView) findViewById(R.id.listView1);
    mAutoOpen = (CheckBox) findViewById(R.id.ckAutoOpen); 
    String[] columns = {DownloadManager.COLUMN_LOCAL_FILENAME,DownloadManager.COLUMN_STATUS};
    int[] fields = {android.R.id.text1,android.R.id.text2};
    mAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, null, columns, fields);
    mList.setAdapter(mAdapter);
    requery();
  }

  public void requery() {
    DownloadManager.Query query = new Query();
    mAdapter.swapCursor(dm.query(query));
  }
  
  public void clickView(View v) {
    Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
    startActivity(intent);
  }
  
  public void clickDownload(View v) {
    try {
      Uri source = Uri.parse(mUrl.getText().toString());
      DownloadManager.Request r = new DownloadManager.Request(source);
      r.setDescription("Download Manager Example");
      r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, source.getLastPathSegment());
      long id=dm.enqueue(r);
      Toast.makeText(this, "download queued", Toast.LENGTH_SHORT).show();
      mInfo.setText("Queued. ID="+id);
      IntentFilter filter = new IntentFilter(
          DownloadManager.ACTION_DOWNLOAD_COMPLETE);
      registerReceiver(new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
          Toast.makeText(context, intent.toString(), Toast.LENGTH_SHORT).show();
          long fileId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
          mInfo.setText(intent.toString()+" fileId="+fileId);
          requery();
          if (fileId>=0 && mAutoOpen.isChecked()) {
            try {
              Intent di = new Intent(Intent.ACTION_VIEW);
              Uri download = dm.getUriForDownloadedFile(fileId);
              String mimetype = dm.getMimeTypeForDownloadedFile(fileId);
              di.setDataAndType(download, mimetype);
              startActivity(di);
            } catch (Exception e) {
              Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
          }
        }
      }, filter);
    } catch (Throwable e) {
      Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }
  }
}
