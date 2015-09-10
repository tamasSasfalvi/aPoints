package hu.gdf.terepimeres.activity.project;

import hu.gdf.terepimeres.Context;
import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.activity.Constants;
import hu.gdf.terepimeres.activity.NewProject;
import hu.gdf.terepimeres.activity.Open;
import hu.gdf.terepimeres.activity.gcpEditor.GcpEditor;
import hu.gdf.terepimeres.activity.project.TwoLineAdapter.ViewHolder;
import hu.gdf.terepimeres.dialog.InputDialog;
import hu.gdf.terepimeres.dialog.InputDialog.InputDialogListener;
import hu.gdf.terepimeres.dialog.MessageDialog;
import hu.gdf.terepimeres.dialog.YesNoDialog;
import hu.gdf.terepimeres.dialog.YesNoDialog.YesNoDialogListener;
import hu.gdf.terepimeres.entity.Gcp;
import hu.gdf.terepimeres.entity.Tracklog;
import hu.gdf.terepimeres.file.kml.KML;
import hu.gdf.terepimeres.service.Tracker;
import hu.gdf.terepimeres.service.Tracker.LocalBinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Project extends FragmentActivity implements OnItemClickListener, InputDialogListener, ServiceConnection, YesNoDialogListener {
  
  private ListView     list;
  private Menu         menu;
  private LinearLayout trackerController;
  private boolean      mBound = false;
  private LocalBinder  tracker;
  private Button       trackerStartPause;
  private TextView     empty;
  
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
    setContentView(R.layout.project);
    list = ((ListView) findViewById(R.id.project_list));
    list.setEmptyView(findViewById(R.id.project_empty)); // set még mielőtt lekérdeznénk (NPE)
    empty = (TextView) list.getEmptyView();
    trackerController = (LinearLayout) findViewById(R.id.tracker_controller); // init mielőtt bindolna a service (NPE)
    trackerStartPause = ((Button) findViewById(R.id.tracker_start_pause));
    
    list.setOnItemClickListener(this);
    registerForContextMenu(list);
    
    File workspace = getExternalFilesDir(null);
    Context.workspace = workspace;
    Context.clearTempDir();
    Context.setLocale(getResources().getConfiguration().locale);
    
    if ( workspace == null ) {
      MessageDialog.newInstance(getString(R.string.dialog_ext_stor_title),
                                getString(R.string.dialog_ext_stor_message))
                   .show(getSupportFragmentManager(), "dialog");
    } else {
      Intent intent = new Intent(this, Open.class);
      startActivityForResult(intent, Constants.OPEN_PROJECT_REQ);
    }
    
    Intent intent = new Intent(this, Tracker.class);
    bindService(intent, this, BIND_AUTO_CREATE);
    
  }
  
  /**
   * Megnyit és betölt egy projektet, majd beállítja azt utolsóként használtként
   * 
   * @param name a projekt neve
   */
  private void openProject(String name) {
    empty.setText(getText(R.string.loading));
    setTitle(name);
    new AsyncTask<File, Void, Void>() {
      
      @Override
      protected Void doInBackground(File... params) {
        Context.kml = KML.newInstance(params[0]);
        return null;
      }
      
      @Override
      protected void onPostExecute(Void result) {
        display();
        updateAddGcpButtonState();
      }
    }.execute(new File(getExternalFilesDir(null),
                       name.concat("/")
                           .concat(name)
                           .concat(hu.gdf.terepimeres.file.kml.Constants.KML_FILE_EXTENSION)));
    
    Context.projectName = name;
    
    trackerController.setVisibility(View.VISIBLE);
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    ProjectItem projectItem = ((ViewHolder) ((AdapterContextMenuInfo) item.getMenuInfo()).targetView.getTag()).projectItem;
    long id = projectItem.getId();
    switch (item.getItemId()) {
      case R.id.context_opendelete_open:
        if ( projectItem instanceof Gcp ) {
          editGcp(id);
        } else if ( projectItem instanceof Tracklog ) {
          File tempTracklog = new File(Context.getTempDir(), projectItem.getName() + ".kml");
          try {
            Context.kml.exportTracklog(tempTracklog, id);
            openKml(tempTracklog);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
        return true;
      case R.id.context_opendelete_delete:
        File workspace = getExternalFilesDir(null);
        if ( workspace == null ) {
          MessageDialog.newInstance(getString(R.string.dialog_ext_stor_title),
                                    getString(R.string.dialog_ext_stor_message))
                       .show(getSupportFragmentManager(), "dialog");
          return true;
        }
        if ( projectItem instanceof Gcp ) {
          Context.kml.removeGcp(id);
        } else if ( projectItem instanceof Tracklog ) {
          Context.kml.removeTracklog(id);
        }
        save();
        return true;
        
      default:
        return super.onContextItemSelected(item);
    }
  }
  
  private void editGcp(long id) {
    Intent intent = new Intent(this, GcpEditor.class);
    intent.putExtra(Constants.GCP_ID, id);
    startActivityForResult(intent, Constants.EDIT_GCP_REQ);
  }
  
  private void display() {
    List<Gcp> gcpList = Context.kml.getGcpData();
    List<Tracklog> trackLogList = Context.kml.getTracklogData();
    List<ProjectItem> elements = new ArrayList<ProjectItem>(gcpList);
    elements.addAll(trackLogList);
    if ( elements.isEmpty() ) {
      ((TextView) list.getEmptyView()).setText(getString(R.string.no_data));
    }
    list.setAdapter(new TwoLineAdapter(this, elements));
  }
  
  @Override
  public void onBackPressed() {
    if ( tracker != null && tracker.isTracking() ) {
      YesNoDialog sureDialog = YesNoDialog.newInstance(getString(R.string.exit_sure_title),
                                                       getString(R.string.exit_sure_message),
                                                       getString(R.string.yes),
                                                       getString(R.string.cancel));
      sureDialog.setListener(this);
      
      sureDialog.show(getSupportFragmentManager(), "exit");
    } else {
      super.onBackPressed();
    }
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if ( mBound ) {
      unbindService(this);
      mBound = false;
    }
  }
  
  @Override
  protected void onStop() {
    super.onStop();
  }
  
  @Override
  protected void onResume() {
    updateAddGcpButtonState();
    super.onResume();
  }
  
  private void updateAddGcpButtonState() {
    if ( Context.kml != null && menu != null ) {
      menu.findItem(R.id.action_add).setEnabled(true);
    }
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu,
                                  View v,
                                  ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.context_open_delete, menu);
  }
  
  @Override
  protected void onActivityResult(int requestCode,
                                  int resultCode,
                                  Intent data) {
    if ( requestCode == Constants.OPEN_PROJECT_REQ || requestCode == Constants.NEW_PROJECT_REQ ) {
      if ( resultCode == RESULT_OK ) {
        // itt nem muszály ellenőrizni a visszatérési értéket, mivel az Openproject ellenörzi a ".project" fájl meglétét
        openProject(data.getExtras().getString(Constants.PROJECT_NAME));
      } else if ( resultCode == RESULT_CANCELED && Context.projectName == null ) {
        finish();
      }
    } else if ( requestCode == Constants.EDIT_GCP_REQ ) {
      if ( resultCode == RESULT_OK ) {
        save();
      }
    }
  }
  
  private void save() {
    try {
      Context.kml.save();
    } catch (IOException e) {
      // nem sikerült menteni
      MessageDialog.newInstance(getString(R.string.dialog_ext_stor_title),
                                getString(R.string.dialog_ext_stor_message))
                   .show(getSupportFragmentManager(), "dialog");
    } catch (TransformerException e) {
      // xml logikai hiba
      MessageDialog.newInstance(getString(R.string.not_saved_title),
                                getString(R.string.not_saved_message))
                   .show(getSupportFragmentManager(), "dialog");
    }
    display();
  }
  
  public void onStartTracking(View v) {
    if ( !tracker.isTracking() ) {
      tracker.start(Context.kml);
      display();
    } else if ( tracker.isPaused() ) {
      tracker.resume();
    } else {
      tracker.pause();
    }
    updateTrackerStartPause();
  }
  
  private void updateTrackerStartPause() {
    if ( !tracker.isTracking() ) {
      trackerStartPause.setText(getString(R.string.tracker_start));
    } else if ( tracker.isPaused() ) {
      trackerStartPause.setText(getString(R.string.tracker_resume));
    } else {
      trackerStartPause.setText(getString(R.string.tracker_pause));
    }
  }
  
  public void onStopTracking(View v) {
    if ( !tracker.isTracking() ) {
      return;
    }
    trackerStartPause.setText(getString(R.string.tracker_start));
    InputDialog.newInstance(getString(R.string.tracklog_name_title),
                            getString(R.string.tracklog_name_message),
                            getString(R.string.save),
                            getString(R.string.dont_save)).show(getSupportFragmentManager(),
                                                                "inputDialog");
    tracker.stop();
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_new_project:
        startActivityForResult(new Intent(this, NewProject.class), Constants.NEW_PROJECT_REQ);
        return true;
      case R.id.action_open_project:
        startActivityForResult(new Intent(this, Open.class), Constants.OPEN_PROJECT_REQ);
        return true;
      case R.id.action_add:
        startActivityForResult(new Intent(this, GcpEditor.class), Constants.EDIT_GCP_REQ);
        return true;
      case R.id.open_kml:
        File kmlFile = new File(Context.kml.getPath());
        openKml(kmlFile);
        return true;
      case R.id.share:
        File project = Context.kml.getParentFile();
        new AsyncTask<File, Void, File>() {
          
          @Override
          protected File doInBackground(File... params) {
            try {
              return Context.kml.exportKmz(Context.getTempDir());
            } catch (IOException e) {
              // nem sikerült menteni
              MessageDialog.newInstance(getString(R.string.dialog_ext_stor_title),
                                        getString(R.string.dialog_ext_stor_message))
                           .show(getSupportFragmentManager(), "dialog");
            } catch (TransformerException e) {
              // xml logikai hiba
              MessageDialog.newInstance(getString(R.string.not_saved_title),
                                        getString(R.string.not_saved_message))
                           .show(getSupportFragmentManager(), "dialog");
            }
            return null;
          }
          
          @Override
          protected void onPostExecute(File result) {
            if ( result != null ) {
              Intent sendIntent = new Intent();
              sendIntent.setAction(Intent.ACTION_SEND);
              sendIntent.setType("*/*");
              sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(result));
              startActivity(Intent.createChooser(sendIntent, "Share"));
            }
          }
          
        }.execute(project);
        
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  private void openKml(File file) {
    Intent open_earth = new Intent(android.content.Intent.ACTION_VIEW);
    open_earth.setDataAndType(Uri.fromFile(file), "application/vnd.google-earth.kml+xml");
    try {
      startActivity(open_earth);
    } catch (ActivityNotFoundException ex) {
      open_earth.setDataAndType(Uri.fromFile(file), "text/xml");
      startActivity(Intent.createChooser(open_earth, "open kml"));
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.project, menu);
    this.menu = menu;
    updateAddGcpButtonState();
    return true;
  }
  
  @Override
  public void onItemClick(AdapterView<?> arg0,
                          View v,
                          int arg2,
                          long arg3) {
    ProjectItem projectItem = ((ViewHolder) v.getTag()).projectItem;
    if ( projectItem instanceof Gcp ) {
      editGcp(projectItem.getId());
    }
  }
  
  @Override
  public void onInputPositiveClick(String input) {
    Context.kml.setTracklogName(input);
    save();
  }
  
  @Override
  public void onInputNegativeClick() {
    Context.kml.deleteTracklog();
    save();
  }
  
  @Override
  public void onServiceConnected(ComponentName arg0,
                                 IBinder service) {
    tracker = (LocalBinder) service;
    mBound = true;
    updateTrackerStartPause();
  }
  
  @Override
  public void onServiceDisconnected(ComponentName arg0) {
    mBound = false;
  }
  
  @Override
  public void onPositiveClick() {
    tracker.stop();
    super.onBackPressed();
  }
  
  @Override
  public void onNegativeClick() {
    // nem reagálunk
  }
}
