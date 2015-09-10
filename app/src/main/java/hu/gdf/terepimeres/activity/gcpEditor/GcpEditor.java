package hu.gdf.terepimeres.activity.gcpEditor;

import hu.gdf.terepimeres.Context;
import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.activity.Constants;
import hu.gdf.terepimeres.entity.Gcp;
import hu.gdf.terepimeres.file.Utils;
import hu.gdf.terepimeres.location.LocationProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class GcpEditor extends FragmentActivity implements LocationListener {
  
  private static LocationProvider locationProvider;
  private Location                bestLocation;
  private EditText                timeView;
  private EditText                accView;
  private EditText                nameView;
  private EditText                lonView;
  private EditText                latView;
  private EditText                altView;
  private LinearLayout            imagesLayout;
  private boolean                 editMode;
  private Gcp                     gcp;
  private GpsFillButton           gpsFillButton;
  private final static int        IMAGE_SIZE = 128;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if ( Context.DEVELOPER_MODE ) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                                                                      .penaltyLog()
                                                                      .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
      // .detectLeakedClosableObjects()
                                                              .penaltyLog()
                                                              .penaltyDeath()
                                                              .build());
    }
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.gcp_editor);
    setupActionBar();
    timeView = (EditText) findViewById(R.id.gcp_editor_time);
    accView = (EditText) findViewById(R.id.gcp_editor_accuracy);
    nameView = (EditText) findViewById(R.id.gcp_editor_name);
    lonView = (EditText) findViewById(R.id.gcp_editor_longitude);
    latView = (EditText) findViewById(R.id.gcp_editor_latitude);
    altView = (EditText) findViewById(R.id.gcp_editor_altitude);
    gpsFillButton = (GpsFillButton) findViewById(R.id.gcp_editor_gps_fill_button);
    imagesLayout = (LinearLayout) findViewById(R.id.images);
    
    locationProvider = new LocationProvider(this, this);
    gpsFillButton.setFragmentActivity(this);
    gpsFillButton.setLocationProvider(locationProvider);
    
    Bundle bundle = getIntent().getExtras();
    if ( bundle == null ) {
      gcp = new Gcp(System.currentTimeMillis());
      editMode = false;
      setTitle(getString(R.string.title_activity_new_gcp));
    } else {
      gcp = Context.kml.getGcpData(bundle.getLong(Constants.GCP_ID));
      nameView.setText(gcp.getName());
      double lon = gcp.getLocation().getLon();
      if ( lon != 0 ) {
        lonView.setText(lon + "");
      }
      double lat = gcp.getLocation().getLat();
      if ( lat != 0 ) {
        latView.setText(lat + "");
      }
      double alt = gcp.getLocation().getAlt();
      if ( alt != 0 ) {
        altView.setText(alt + "");
      }
      timeView.setText(gcp.getTime());
      accView.setText(gcp.getAccuracy() + "");
      Bitmap bm;
      for (File img : gcp.getImgs()) {
        if ( img.exists()
            && (bm = hu.gdf.terepimeres.activity.Utils.getBitmap(img.getAbsolutePath(), IMAGE_SIZE)) != null ) {
          addImg(bm);
        }
      }
      
      editMode = true;
      setTitle(getString(R.string.title_activity_edit_gcp));
    }
  }
  
  private void addImg(Bitmap img) {
    if ( img != null ) {
      ImageView view = new ImageView(this);
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.gpsFill_and_img_width),
                                                                             (int) getResources().getDimension(R.dimen.gpsFill_and_img_width));
      layoutParams.leftMargin = 2;
      layoutParams.rightMargin = 2;
      view.setLayoutParams(layoutParams);
      view.setScaleType(ScaleType.CENTER_CROP);
      view.setImageBitmap(img);
      imagesLayout.addView(view);
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    if ( locationProvider != null ) {
      locationProvider.stopReceiveUpdates();
    }
  }
  
  @Override
  protected void onActivityResult(int requestCode,
                                  int resultCode,
                                  Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    
    switch (requestCode) {
      case Constants.GET_PICTURE:
        if ( resultCode == RESULT_OK ) {
          Uri selectedImage;
          File pic;
          if ( intent != null ) {
            if ( (selectedImage = intent.getData()) != null ) { // galery
              String[] filePathColumn = { MediaStore.Images.Media.DATA };
              
              Cursor cursor = getContentResolver().query(selectedImage,
                                                         filePathColumn,
                                                         null,
                                                         null,
                                                         null);
              cursor.moveToFirst();
              
              int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
              String filePath = cursor.getString(columnIndex);
              cursor.close();
              pic = copyImgToProject(new File(filePath), false, false);
              if ( gcp.addImg(pic) ) {
                addImg(hu.gdf.terepimeres.activity.Utils.getBitmap(pic.getAbsolutePath(),
                                                                   IMAGE_SIZE));
              }
            } else { // camera
              pic = copyImgToProject(Context.getTempPic(), true, true);
              if ( gcp.addImg(pic) ) {
                addImg(hu.gdf.terepimeres.activity.Utils.getBitmap(pic.getAbsolutePath(),
                                                                   IMAGE_SIZE));
              }
            }
          } else { // ki válaszol intent nélkül?
            pic = copyImgToProject(Context.getTempPic(), false, true);
            if ( gcp.addImg(pic) ) {
              addImg(hu.gdf.terepimeres.activity.Utils.getBitmap(pic.getAbsolutePath(), IMAGE_SIZE));
            }
          }
        }
    }
  }
  
  private File copyImgToProject(File source,
                                boolean tryReplace,
                                boolean generateName) {
    final File projPic = new File(Context.getProjectDirectory(), generateName
        ? System.currentTimeMillis() + hu.gdf.terepimeres.Constants.JPEG_EXTENSION
        : source.getName());
    
    if ( !tryReplace || !Context.getTempPic().renameTo(projPic) ) {
      Utils.copyFile(source, projPic);
    }
    return projPic;
  }
  
  // kép beállítása
  public void onAddImg(View v) {
    
    final List<Intent> cameraIntents = new ArrayList<Intent>();
    if ( hu.gdf.terepimeres.activity.Utils.isIntentAvailable(this,
                                                             android.provider.MediaStore.ACTION_IMAGE_CAPTURE) ) {
      
      final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
      final PackageManager packageManager = getPackageManager();
      final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
      for (ResolveInfo res : listCam) {
        final String packageName = res.activityInfo.packageName;
        final Intent intent = new Intent(captureIntent);
        intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
        intent.setPackage(packageName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Context.getTempPic()));
        cameraIntents.add(intent);
      }
      
    }
    
    final Intent photoPickerIntent = new Intent();
    photoPickerIntent.setAction(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    
    final Intent chooserIntent = Intent.createChooser(photoPickerIntent,
                                                      getString(R.string.select_app));
    
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));
    
    startActivityForResult(chooserIntent, Constants.GET_PICTURE);
  }
  
  // GPS update kérés gomb eseménykezelője
  public void onGpsFill(View v) {
    Log.i(this.getClass().toString(), "onGpsUpdate");
    if ( gpsFillButton.isChecked() ) {
      locationProvider.getLocaionUpdates(1000, 0);
    } else {
      locationProvider.stopReceiveUpdates();
    }
  }
  
  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void setupActionBar() {
    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }
  
  public void onDone(View v) {
    gcp.setName(nameView.getText().toString());
    String lonVal = lonView.getText().toString();
    String latVal = latView.getText().toString();
    String altVal = altView.getText().toString();
    String accVal = accView.getText().toString();
    String timeVal = timeView.getText().toString();
    hu.gdf.terepimeres.entity.Location location = new hu.gdf.terepimeres.entity.Location("".equals(lonVal)
                                                                                             ? 0
                                                                                             : Double.parseDouble(lonVal),
                                                                                         "".equals(latVal)
                                                                                             ? 0
                                                                                             : Double.parseDouble(latVal),
                                                                                         "".equals(altVal)
                                                                                             ? 0
                                                                                             : Double.parseDouble(altVal));
    gcp.setLocation(location);
    gcp.setAccuracy("".equals(accVal) ? 0 : Double.parseDouble(accVal));
    gcp.setTime("".equals(timeVal) ? "" : timeVal);
    
    if ( editMode ) {
      Context.kml.editGcp(gcp);
    } else {
      Context.kml.addGcp(gcp);
    }
    finishOK();
  }
  
  private void finishOK() {
    setResult(RESULT_OK);
    finish();
  }
  
  @Override
  public void onLocationChanged(Location location) {
    Log.i(this.getClass().toString(), "onLocationChanged");
    double lon = location.getLongitude();
    double lat = location.getLatitude();
    if ( bestLocation == null || bestLocation.getAccuracy() <= location.getAccuracy() ) {
      this.lonView.setText(lon + "");
      this.latView.setText(lat + "");
      altView.setText(location.getAltitude() + "");
      String date = Context.getShortDateTimeFormat().format(location.getTime());
      timeView.setText(date);
      accView.setText(location.getAccuracy() + "");
    }
  }
  
  @Override
  public void onStatusChanged(String provider,
                              int status,
                              Bundle extras) {
  }
  
  @Override
  public void onProviderEnabled(String provider) {
  }
  
  @Override
  public void onProviderDisabled(String provider) {
  }
}
