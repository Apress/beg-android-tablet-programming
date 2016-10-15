package com.apress.ba3tp.remindme;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabase extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "smsdb";
  public static final int DATABASE_VERSION = 4;
  public AlarmDatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table alarms (_id integer primary key autoincrement," +
        "phone text,message text, trigger datetime, enabled boolean, status text, lastupdate datetime)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
    if (newversion>oldversion) {
      db.execSQL("drop table alarms");
      onCreate(db);
    }
  }
  
  public static int getLastId(SQLiteDatabase db) {
    Cursor c = db.rawQuery("select last_insert_rowid()", null);
    c.moveToFirst();
    int result = c.getInt(0);
    c.close();
    return result;
  }
}
