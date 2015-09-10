package hu.gdf.terepimeres.activity;

import hu.gdf.terepimeres.Context;
import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.dialog.MessageDialog;
import hu.gdf.terepimeres.dialog.YesNoDialog;
import hu.gdf.terepimeres.dialog.YesNoDialog.YesNoDialogListener;
import hu.gdf.terepimeres.file.Utils;
import hu.gdf.terepimeres.file.kml.KML;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class NewProject extends FragmentActivity {
  
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
    setContentView(R.layout.new_project);
    // Show the Up button in the action bar.
    setupActionBar();
  }
  
  // a Create gomb eseménykezelője
  public void onCreateProject(View v) throws ParserConfigurationException, SAXException, IOException {
    String name = ((EditText) findViewById(R.id.proj_name)).getText().toString();
    if ( "".equals(name) ) {
      MessageDialog.newInstance(getString(R.string.missing_data_title),
                                getString(R.string.missing_data_message_name))
                   .show(getSupportFragmentManager(), "dialog");
      return;
    }
    File workspace = getExternalFilesDir(null);
    final File dir = new File(workspace, name);
    final File kmlFile = new File(dir,
                                  name.concat(hu.gdf.terepimeres.file.kml.Constants.KML_FILE_EXTENSION));
    if ( kmlFile.exists() ) {
      YesNoDialog sureDialog = YesNoDialog.newInstance(getString(R.string.overwrite_title),
                                                       getString(R.string.overwrite_message),
                                                       getString(R.string.yes),
                                                       getString(R.string.no));
      sureDialog.setListener(new YesNoDialogListener() {
        
        @Override
        public void onPositiveClick() {
          Utils.deleteDir(dir, true);// le kell törölnünk a könyvtárat is, hogy újra létrejöhessen a
                                     // projectkonfigurációs fájl
          createNewProject(kmlFile);// csak akkor hozza létre a projektkonfigurációs fájlt, ha a projektkönyvtár nem
                                    // létezik
        }
        
        @Override
        public void onNegativeClick() {
          // do nothing
        }
      });
      
      sureDialog.show(getSupportFragmentManager(), "exists");
    } else {
      createNewProject(kmlFile);
    }
  }
  
  private void createNewProject(File kmlFile) {
    File dir = kmlFile.getParentFile();
    try {
      if ( !dir.exists() ) {
        dir.mkdir();
        // ebben taároljuk a projektspecifikus paramétereket
        File projectConfigFile = new File(dir, hu.gdf.terepimeres.Constants.PROJECT_FILE_NAME);
        projectConfigFile.createNewFile();
      }
      
      KML kml = KML.newInstance(kmlFile);
      kml.setDescription(((EditText) findViewById(R.id.proj_descr)).getText().toString(),
                         ((EditText) findViewById(R.id.proj_team)).getText().toString());
      kml.save();
      setResult(RESULT_OK,
                new Intent().putExtra(Constants.PROJECT_NAME,
                                      Utils.removeExtension(kmlFile.getName())));
      finish();
    } catch (IOException e) {
      // sikertelen mentés
      MessageDialog.newInstance(getString(R.string.dialog_ext_stor_title),
                                getString(R.string.dialog_ext_stor_message))
                   .show(getSupportFragmentManager(), "dialog");
    } catch (TransformerException e) {
      // xml logikai hiba
      MessageDialog.newInstance(getString(R.string.not_saved_title),
                                getString(R.string.not_saved_message))
                   .show(getSupportFragmentManager(), "dialog");
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
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // This ID represents the Home or Up button. In the case of this
        // activity, the Up button is shown. Use NavUtils to allow users
        // to navigate up one level in the application structure. For
        // more details, see the Navigation pattern on Android Design:
        //
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        //
        NavUtils.navigateUpFromSameTask(this);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
