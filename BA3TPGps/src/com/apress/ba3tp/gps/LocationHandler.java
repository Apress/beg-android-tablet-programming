package com.apress.ba3tp.gps;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationHandler implements LocationListener {
  private final Context mContext;
  private final Map<String, Location> mLocationUpdates;
  private final Map<String, String> mStatus;
  private final LocationManager mLocationManager;
  private final Geocoder mGeocoder;
  private LocationUpdater mUpdater;

  @Override
  public synchronized void onLocationChanged(Location location) {
    mLocationUpdates.put(location.getProvider(), location);
    if (mUpdater != null)
      mUpdater.locationUpdate(this, location);
  }

  @Override
  public void onProviderDisabled(String provider) {
  }

  @Override
  public void onProviderEnabled(String provider) {
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    String s;
    switch(status) {
    case LocationProvider.AVAILABLE: s="Available"; break;
    case LocationProvider.TEMPORARILY_UNAVAILABLE: s="Unavailable (Temp)"; break;
    case LocationProvider.OUT_OF_SERVICE: s="Out of Service"; break;
    default: s="Unknown("+status+")";
    }
    if (extras!=null && extras.containsKey("satellites")) {
      s+="("+extras.getInt("satellites")+" sats)";
    }
    mStatus.put(provider, s);
  }

  public LocationHandler(Context context) {
    mContext = context;
    mGeocoder = new Geocoder(mContext);
    mLocationManager = (LocationManager) mContext
        .getSystemService(Context.LOCATION_SERVICE);
    mLocationUpdates = new HashMap<String, Location>();
    mStatus = new HashMap<String,String>();
  }

  public void shutdown() {
    stopLocating();
  }

  public void startLocating(int minUpdateTime, int minUpdateDistance) {
    for (String provider : mLocationManager.getAllProviders()) {
      mLocationManager.requestLocationUpdates(provider, minUpdateTime,
          minUpdateDistance, this, mContext.getMainLooper());
      Location loc = mLocationManager.getLastKnownLocation(provider);
      if (loc!=null) mLocationUpdates.put(provider, loc);
    }
  }

  public List<String> getProviders() {
    return mLocationManager.getAllProviders();
  }

  public Map<String, Location> readLocation() {
    return mLocationUpdates;
  }

  public Map<String, String> readStatus() {
    return mStatus;
  }

  public synchronized void stopLocating() {
    mLocationManager.removeUpdates(this);
    mLocationUpdates.clear();
  }

  public List<Address> geocode(Double latitude, Double longitude,
      Integer maxResults) throws IOException {
    return mGeocoder.getFromLocation(latitude, longitude, maxResults);
  }

  public void setUpdater(LocationUpdater updater) {
    mUpdater = updater;
  }
  
  public static String getAccuracyName(int accuracy) {
    switch(accuracy) {
    case Criteria.NO_REQUIREMENT: return "No Requirement";
    case Criteria.ACCURACY_COARSE: return "Coarse";
    case Criteria.ACCURACY_FINE: return "Fine";
    default: return "N/A";
    }
  }

  public static String getPowerName(int power) {
    switch(power) {
    case Criteria.NO_REQUIREMENT: return "No Requirement";
    case Criteria.POWER_LOW: return "Low";
    case Criteria.POWER_MEDIUM: return "Medium";
    case Criteria.POWER_HIGH: return "High";
    default: return "N/A";
    }
  }

  public interface LocationUpdater {
    void locationUpdate(LocationHandler handler, Location location);
  }

}
