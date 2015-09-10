package hu.gdf.terepimeres.service;

import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.entity.Tracklog;
import hu.gdf.terepimeres.file.kml.KML;
import hu.gdf.terepimeres.location.LocationProvider;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Tracker extends Service implements LocationListener, Observer {
  
  private static final long   GPS_UPDATE_TIME_INTERVAL     = 3000;
  private static final float  GPS_UPDATE_DISTANCE_INTERVAL = 5;
  private static final int    NOTIFICATION_ID              = 851201;
  private static final float  REQ_ACCURACY                 = GPS_UPDATE_DISTANCE_INTERVAL * 2;
  private LocalBinder         mBinder                      = new LocalBinder();
  private KML                 kml;
  private LocationProvider    locationProvider;
  private Timer               fixTimer                     = new Timer(true);
  private ExecutorService     executorService;
  private boolean             tracking;
  private boolean             paused;
  private static Notification notification;
  private NotificationManager notificationManager;
  private Tracklog            tracklog;
  
  // az utolsó rögzített location
  private Location            lastLoc;
  private TimerTask           fixTask;
  
  @Override
  public void onCreate() {
    executorService = Executors.newSingleThreadExecutor();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
  }
  
  private void start(KML kml) {
    if ( tracking ) {
      return;
    }
    
    this.kml = kml;
    tracklog = new Tracklog(System.currentTimeMillis());
    
    kml.addTrackLog(tracklog);
    if ( locationProvider == null ) {
      locationProvider = new LocationProvider(this, this);
    }
    track();
    
    notification = new NotificationCompat.Builder(this).setContentTitle(getString(R.string.app_name))
                                                       .setContentText("Your track being logged")
                                                       .setContentIntent(PendingIntent.getActivity(this,
                                                                                                   0,
                                                                                                   new Intent(),
                                                                                                   0))
                                                       .setSmallIcon(R.drawable.tracking_icon_level_list)
                                                       .build();
    
    startForeground(NOTIFICATION_ID, notification);
    tracking = true;
    paused = false;
  }
  
  private void track() {
    locationProvider.getLocaionUpdates(GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL);
  }
  
  private void resume() {
    if ( !tracking || !paused ) {
      return;
    }
    track();
    paused = false;
  }
  
  private void pause() {
    if ( !tracking || paused ) {
      return;
    }
    if ( locationProvider != null ) {
      locationProvider.stopReceiveUpdates();
    }
    writeLocations();
    paused = true;
    if ( fixTask != null ) {
      fixTask.cancel();
    }
    notification.iconLevel = 0;
    notificationManager.notify(NOTIFICATION_ID, notification);
  }
  
  private void save() {
    executorService.submit(new Runnable() {
      
      @Override
      public void run() {
        try {
          kml.save();
        } catch (TransformerFactoryConfigurationError e) {
          throw new RuntimeException(e);
        } catch (TransformerException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }
  
  private void writeLocations() {
    if ( executorService == null || executorService.isShutdown() || executorService.isTerminated() ) {
      kml.addDebugPoint("there is no executor - do not write cache", tracklog.getLastLoc());
      try {
        kml.save(); // executorService nélkül a GUI szálban mentünk
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return;
    }
    kml.editTracklog(tracklog);
    save();
  }
  
  private void stop() {
    if ( !tracking ) {
      return;
    }
    if ( locationProvider != null ) {
      locationProvider.stopReceiveUpdates();
    }
    writeLocations();
    stopForeground(true);
    locationProvider = null;
    tracking = false;
    paused = false;
    if ( fixTask != null ) {
      fixTask.cancel();
    }
  }
  
  public boolean isPaused() {
    return paused;
  }
  
  public boolean isTracking() {
    return tracking;
  }
  
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
  
  @Override
  public void onDestroy() {
    if ( tracking && !tracklog.getLocations().isEmpty() ) {
      // kml.addDebugPoint("tracker destroy", tracklog.getLastLoc());
      writeLocations();
    }
    executorService.shutdown();
  }
  
  @Override
  public void onLocationChanged(Location location) {
    if ( location.getAccuracy() < REQ_ACCURACY ) {
      if ( notification.iconLevel != 2 ) {
        notification.iconLevel = 2;
        notificationManager.notify(NOTIFICATION_ID, notification);
      }
      
      final hu.gdf.terepimeres.entity.Location loc = new hu.gdf.terepimeres.entity.Location(location);
      
      /*
       * sajnos jelenleg az Android nem biztosít megbízható eseményt, ami a GPS fix elvesztését jelezné. Ezért minden
       * location érkezése után elindítunk egy időzített feladatot, ami GPS_UPDATE_TIME_INTERVAL * 2 + 1sec idő múlva
       * úgy végrehajtódik.
       */
      if ( fixTask != null && !fixTask.cancel() ) {// a cancel miatt nem szabad kikommentezni
        // kml.addDebugPoint("GPS fix gained", loc);
      }
      fixTask = new TimerTask() {
        
        @Override
        public void run() {
          notifyLostFix();
          // kml.addDebugPoint("GPS fix lost", loc);
        }
      };
      fixTimer.schedule(fixTask, GPS_UPDATE_TIME_INTERVAL * 3);
      
      if ( lastLoc != null && lastLoc.distanceTo(location) < REQ_ACCURACY + 1 ) {
        return;
      }
      
      lastLoc = location;
      
      tracklog.addLocation(loc);
      
      if ( tracklog.getLocations().size() % 32 == 0 ) { // előfordulhat, h nem sikerült menteni, amikor == 32
        writeLocations();
      }
    } else {
      notifyLostFix();
    }
  }
  
  private void notifyLostFix() {
    if ( notification.iconLevel != 1 ) {
      notification.iconLevel = 1;
      notificationManager.notify(NOTIFICATION_ID, notification);
    }
  }
  
  @Override
  public void onStatusChanged(String provider,
                              int status,
                              Bundle extras) {
    if ( tracklog.getLocations().isEmpty() ) {
      return;
    }
    hu.gdf.terepimeres.entity.Location lastLoc = tracklog.getLastLoc();
    switch (status) {
      case android.location.LocationProvider.OUT_OF_SERVICE:
        kml.addDebugPoint("GPS out of service", lastLoc);
        break;
      case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
        kml.addDebugPoint("GPS temporarily unavailable", lastLoc);
        break;
      case android.location.LocationProvider.AVAILABLE:
        //kml.addDebugPoint("GPS became available", lastLoc);
        break;
    }
  }
  
  @Override
  public void onProviderEnabled(String provider) {
    Log.i(this.getClass().toString(), "onProviderEnabled()");
    if ( !tracklog.getLocations().isEmpty() ) {
      kml.addDebugPoint("GPS enabled", tracklog.getLastLoc());
    }
  }
  
  @Override
  public void onProviderDisabled(String provider) {
    Log.i(this.getClass().toString(), "onProviderDisabled()");
    if ( !tracklog.getLocations().isEmpty() ) {
      kml.addDebugPoint("GPS disabled", tracklog.getLastLoc());
    }
  }
  
  @Override
  public void onLowMemory() {
    Log.i(this.getClass().toString(), "onLowMemory()");
    if ( !tracklog.getLocations().isEmpty() ) {
      kml.addDebugPoint("Low memory", tracklog.getLastLoc());
    }
    super.onLowMemory();
  }
  
  public class LocalBinder extends Binder {
    
    public void start(KML kml) {
      Tracker.this.start(kml);
    }
    
    public void pause() {
      Tracker.this.pause();
    }
    
    public void stop() {
      Tracker.this.stop();
    }
    
    public boolean isTracking() {
      return Tracker.this.isTracking();
    }
    
    public boolean isPaused() {
      return Tracker.this.isPaused();
    }
    
    public void resume() {
      Tracker.this.resume();
    }
  }

	@Override
  public void update(Observable observable,
                     Object data) {
	  // TODO Auto-generated method stub
	  
  }
}
