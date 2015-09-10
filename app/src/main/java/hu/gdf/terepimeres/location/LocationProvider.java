package hu.gdf.terepimeres.location;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class LocationProvider {
  
  private final LocationManager  locationManager;
  private final LocationListener locationListener;
  
  public LocationProvider(ContextWrapper activity, LocationListener locationListener) {
    this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    this.locationListener = locationListener;
  }
  
  public void getLocaionUpdates(long minTime,
                                float minDist) {
    try {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                             minTime,
                                             minDist,
                                             locationListener);
    } catch (final IllegalArgumentException ex) {
      Log.e(this.getClass().toString(), "", ex);
      throw ex;
    }
  }
  
  public void stopReceiveUpdates() {
    locationManager.removeUpdates(locationListener);
  }
  
  public void getOneShotGpsLocation() {
    Log.i(getClass().toString(), "getOneShotGpsLocation");
    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
  }
  
  public boolean isGpsEnabled() {
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }
}
