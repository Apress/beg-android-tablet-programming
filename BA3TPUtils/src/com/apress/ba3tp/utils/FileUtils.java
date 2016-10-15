/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.apress.ba3tp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import android.util.StringBuilderPrinter;

/**
 * Utility functions for handling files.
 */
public class FileUtils {

  // Linux stat constants
  public static final int S_IFMT = 0170000; /* type of file */
  public static final int S_IFLNK = 0120000; /* symbolic link */
  public static final int S_IFREG = 0100000; /* regular */
  public static final int S_IFBLK = 0060000; /* block special */
  public static final int S_IFDIR = 0040000; /* directory */
  public static final int S_IFCHR = 0020000; /* character special */
  public static final int S_IFIFO = 0010000; /* this is a FIFO */
  public static final int S_ISUID = 0004000; /* set user id on execution */
  public static final int S_ISGID = 0002000; /* set group id on execution */

  private FileUtils() {
    // Utility class.
  }

  public static int chmod(File path, int mode) throws Exception {
    Class<?> fileUtils = Class.forName("android.os.FileUtils");
    Method setPermissions =
        fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
    return (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
  }

  public static int getPermissions(File path) throws Exception {
    Class<?> fileUtils = Class.forName("android.os.FileUtils");
    int[] result = new int[1];
    Method getPermissions = fileUtils.getMethod("getPermissions", String.class, int[].class);
    getPermissions.invoke(null, path.getAbsolutePath(), result);
    return result[0];
  }

  public static FileStatus getFileStatus(File path) throws Exception {
    FileStatus result = new FileStatus();
    Class<?> fileUtils = Class.forName("android.os.FileUtils");
    Class<?> fileStatus = Class.forName("android.os.FileUtils$FileStatus");
    Method getOsFileStatus = fileUtils.getMethod("getFileStatus", String.class, fileStatus);
    Object fs = fileStatus.newInstance();
    if ((Boolean) getOsFileStatus.invoke(null, path.getAbsolutePath(), fs)) {
      result.atime = fileStatus.getField("atime").getLong(fs);
      result.blksize = fileStatus.getField("blksize").getInt(fs);
      result.blocks = fileStatus.getField("blocks").getLong(fs);
      result.ctime = fileStatus.getField("ctime").getLong(fs);
      result.dev = fileStatus.getField("dev").getInt(fs);
      result.gid = fileStatus.getField("gid").getInt(fs);
      result.ino = fileStatus.getField("ino").getInt(fs);
      result.mode = fileStatus.getField("mode").getInt(fs);
      result.mtime = fileStatus.getField("mtime").getLong(fs);
      result.nlink = fileStatus.getField("nlink").getInt(fs);
      result.rdev = fileStatus.getField("rdev").getInt(fs);
      result.size = fileStatus.getField("size").getLong(fs);
      result.uid = fileStatus.getField("uid").getInt(fs);
    }
    return result;
  }
  
  private static String permRwx(int perm) {
    String result;
    result = ((perm & 04) != 0 ? "r" : "-") + ((perm & 02) != 0 ? "w" : "-")
        + ((perm & 1) != 0 ? "x" : "-");
    return result;
  }

  private static String permFileType(int perm) {
    String result = "?";
    switch (perm & S_IFMT) {
    case S_IFLNK:
      result = "s";
      break; /* symbolic link */
    case S_IFREG:
      result = "-";
      break; /* regular */
    case S_IFBLK:
      result = "b";
      break; /* block special */
    case S_IFDIR:
      result = "d";
      break; /* directory */
    case S_IFCHR:
      result = "c";
      break; /* character special */
    case S_IFIFO:
      result = "p";
      break; /* this is a FIFO */
    }
    return result;
  }

  public static String permString(int perms) {
    String result;
    result = permFileType(perms) + permRwx(perms >> 6) + permRwx(perms >> 3)
        + permRwx(perms);
    return result;
  }

  public static String getExtension(File f) {
    if (f==null) return null;
    String name = f.getName();
    int dotpos = name.lastIndexOf(".");
    if (dotpos>=0) return name.substring(dotpos+1);
    else return null;
  }

  // Execute a shell command and return the result as a String
  public static String execCmd(String cmd) {
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedReader r = new BufferedReader(new InputStreamReader(
          p.getInputStream()));
      StringBuilder builder = new StringBuilder();
      StringBuilderPrinter sp = new StringBuilderPrinter(builder);
      String line;
      while ((line = r.readLine()) != null) {
        sp.println(line);
      }
      r.close();
      return builder.toString();
    } catch (Exception e) {
      return e.toString();
    }
  }
  

  public static class FileStatus {
    public long atime;
    public int blksize;
    public long blocks;
    public long ctime;
    public int dev;
    public int gid;
    public int ino;
    public int mode;
    public long mtime;
    public int nlink;
    public int rdev;
    public long size;
    public int uid;
  }
  
}
