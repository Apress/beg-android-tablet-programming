package com.apress.ba3tp.gps;

import android.location.Location;

public class Target {
  double latitude;
  double longitude;
  private String mTitle;

  Target(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  Target(Location location) {
    this.latitude = location.getLatitude();
    this.longitude = location.getLongitude();
  }

  Target(String location) {
    setAsString(location);
  }

  public String getTitle() {
    if (mTitle == null)
      return latitude + "," + longitude;
    return mTitle;
  }

  public void setTitle(String title) {
    mTitle = title;
  }

  public String getAsString() {
    String s = latitude + "," + longitude;
    if (mTitle != null)
      s = s + "," + (mTitle.replace(",", ";"));
    return s;
  }

  public void setAsString(String location) {
    String[] parts = location.split(",");
    if (parts.length < 2)
      throw new IllegalArgumentException("Expected format lat,long[,title]");
    latitude = Double.valueOf(parts[0]);
    longitude = Double.valueOf(parts[1]);
    if (parts.length >= 3)
      mTitle = parts[2];
  }

  @Override
  public String toString() {
    return getTitle();
  }
}
