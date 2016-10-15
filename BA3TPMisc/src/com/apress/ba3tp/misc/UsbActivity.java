package com.apress.ba3tp.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UsbActivity extends Activity {
  ListView mList;
  UsbManager mUsbManager;
  UsbEndpoint mUsbEndpoint;
  MyReceiver mReceiver = new MyReceiver();
  UsbDeviceConnection mConn = null;
  Handler mHandler;
  TextView mInfo;
  static public final int VENDOR_ID = 6465; // These work for my test USB music
                                            // keyboard.
  static public final int PRODUCT_ID = 32801;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.usb);
    mList = (ListView) findViewById(R.id.listView1);
    mInfo = (TextView) findViewById(R.id.eInfo);
    mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
    mHandler = new Handler();
    clickList(null);
  }

  @Override
  protected void onPause() {
    unregisterReceiver(mReceiver);
    closeConn();
    super.onPause();
  }

  @Override
  protected void onResume() {
    registerReceiver(mReceiver, new IntentFilter(
        UsbManager.ACTION_USB_DEVICE_DETACHED));
    registerReceiver(mReceiver, new IntentFilter(
        UsbManager.ACTION_USB_DEVICE_ATTACHED));
    registerReceiver(mReceiver, new IntentFilter(
        UsbManager.ACTION_USB_ACCESSORY_ATTACHED));
    registerReceiver(mReceiver, new IntentFilter(
        UsbManager.ACTION_USB_ACCESSORY_DETACHED));
    super.onResume();
  }

  public void clickList(View v) {
    Map<String, UsbDevice> mylist = mUsbManager.getDeviceList();
    List<String> values = new ArrayList<String>(mylist.size());
    if (mylist != null && mylist.size() > 0) {
      for (String key : mylist.keySet()) {
        StringBuilder b = new StringBuilder();
        UsbDevice device = mylist.get(key);
        b.append(device.toString() + "\n");
        for (int i = 0; i < device.getInterfaceCount(); i++) {
          UsbInterface usbint = device.getInterface(i);
          b.append("  " + usbint + "\n");
          for (int j = 0; j < usbint.getEndpointCount(); j++) {
            UsbEndpoint endpoint = usbint.getEndpoint(j);
            b.append("    " + endpoint + "\n");
          }
        }
        values.add(b.toString());
        if (device.getVendorId() == VENDOR_ID
            && device.getProductId() == PRODUCT_ID) {
          if (mConn == null) {
            try {
              UsbInterface usbi = device.getInterface(0);
              mUsbEndpoint = usbi.getEndpoint(0);
              mConn = mUsbManager.openDevice(device);
              if (mConn.claimInterface(usbi, true)) {
                startComms();
              } else {
                closeConn();
              }
            } catch (Exception e) {
              toast(e);
            }
          }
        }
      }
    } else {
      values.add("No USB devices found.");
    }
    UsbAccessory[] accessories = mUsbManager.getAccessoryList();
    if (accessories != null) {
      for (UsbAccessory a : accessories) {
        values.add("Accessory: " + a);
      }
    }
    ListAdapter adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, values);
    mList.setAdapter(adapter);
  }

  private void closeConn() {
    if (mConn != null) {
      synchronized(mConn) {
        mConn.close();
        mConn = null;
        mUsbEndpoint = null;
      }
    }
  }
  
  private void startComms() {
    Thread comms = new Thread(new MyComms());
    comms.start();
  }

  private void toast(Object msg) {
    Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show();
  }

  class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Toast.makeText(context, intent.toString(), Toast.LENGTH_SHORT).show();
      closeConn();
      clickList(null); // Refresh the list.
    }
  }
  
  class MyComms implements Runnable {
    
    @Override
    public void run() {
      byte[] buffer = new byte[8]; //Happen to know max size for this device is 8 
      while (mConn!=null) {
        int len=mConn.bulkTransfer(mUsbEndpoint, buffer, buffer.length, 10000); // Wait 10 seconds
        if (len>0) {
          StringBuilder sb = new StringBuilder();
          for(int i=0; i<len; i++) {
            sb.append(String.format("%02x", buffer[i]));
          }
          final String s = sb.toString();
          mHandler.post(new Runnable() {

            @Override
            public void run() {
              mInfo.setText(s);
            }});
        }
      }
    }
    
  }
}
