package hu.gdf.terepimeres.activity;

import hu.gdf.terepimeres.Context;
import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.dialog.MessageDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Open extends FragmentActivity implements OnItemClickListener {
  
  private ListView      list;
  private static String LAST_MOD;
  
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
    setContentView(R.layout.open);
    list = ((ListView) findViewById(R.id.open_list));
    LAST_MOD = getString(R.string.open_last_mod);
    load();
    list.setOnItemClickListener(this);
    list.setEmptyView(findViewById(R.id.open_empty));
    registerForContextMenu(list);
    setupActionBar();
  }
  
  private void load() {
    List<File> projectDirs = getProjectDirectories();
    sortFilesDesc(projectDirs);
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    Map<String, String> item;
    for (File projectDir : projectDirs) {
      item = new HashMap<String, String>();
      item.put("name", projectDir.getName());
      item.put("descName", LAST_MOD);
      item.put("desc", Context.getShortDateFormat().format(new Date(projectDir.lastModified())));
      data.add(item);
    }
    list.setAdapter(new SimpleAdapter(this, data, R.layout.title_desc, new String[] {
        "name",
        "descName",
        "desc" }, new int[] { R.id.title, R.id.desc_name, R.id.desc }));
  }
  
  @Override
  public void onItemClick(AdapterView<?> parent,
                          View v,
                          int position,
                          long id) {
    setResult(RESULT_OK, new Intent().putExtra(hu.gdf.terepimeres.activity.Constants.PROJECT_NAME,
                                               ((TextView) v.findViewById(R.id.title)).getText()
                                                                                      .toString()));
    finish();
  }
  
  /**
   * Az emptyView tartalmaz egy Retry feliratú gombot. Ez az eseménykezelője
   * 
   * @param v a gomb view-ja
   */
  public void onRetry(View v) {
    load();
  }
  
  public static void sortFilesDesc(List<File> files) {
    Collections.sort(files, new Comparator<File>() {
      
      long a;
      long b;
      
      @Override
      public int compare(File lhs,
                         File rhs) {
        a = lhs.lastModified();
        b = rhs.lastModified();
        if ( a < b ) {
          return 1;
        } else if ( b > a ) {
          return -1;
        }
        return 0;
      }
    });
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.open, menu);
    return true;
  }
  
  private List<File> getProjectDirectories() {
    File workspace = getExternalFilesDir(null);
    List<File> projects = new ArrayList<File>();
    if ( workspace == null ) {
      return projects;
    }
    for (File file : workspace.listFiles()) {
      if ( file.isDirectory() ) {
        if ( file.list(new FilenameFilter() {
          
          @Override
          public boolean accept(File dir,
                                String filename) {
            if ( filename.equals(hu.gdf.terepimeres.Constants.PROJECT_FILE_NAME) ) {
              return true;
            }
            return false;
          }
        }).length == 1 ) {
          projects.add(file);
        }
      }
    }
    if ( projects.isEmpty() ) {
      startNewProjectActivity();
    }
    return projects;
  }
  
  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void setupActionBar() {
    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
      // Show the Up button in the action bar.
      getActionBar().setDisplayHomeAsUpEnabled(true);
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
  public boolean onContextItemSelected(MenuItem item) {
    TextView projectNameView = ((TextView) ((AdapterContextMenuInfo) item.getMenuInfo()).targetView.findViewById(R.id.title));
    String projectName = projectNameView.getText().toString();
    switch (item.getItemId()) {
      case R.id.context_opendelete_open:
        setResult(RESULT_OK,
                  new Intent().putExtra(hu.gdf.terepimeres.activity.Constants.PROJECT_NAME,
                                        projectName));
        finish();
        return true;
      case R.id.context_opendelete_delete:
        File workspace = getExternalFilesDir(null);
        if ( workspace == null ) {
          MessageDialog.newInstance(getString(R.string.dialog_ext_stor_title),
                                    getString(R.string.dialog_ext_stor_message))
                       .show(getSupportFragmentManager(), "dialog");
          return true;
        }
        hu.gdf.terepimeres.file.Utils.deleteDir(new File(workspace, projectName), true);
        load();
        return true;
        
      default:
        return super.onContextItemSelected(item);
    }
  }
  
  @Override
  protected void onActivityResult(int requestCode,
                                  int resultCode,
                                  Intent data) {
    if ( requestCode == Constants.NEW_PROJECT_REQ ) {
      if ( resultCode == RESULT_OK ) {
        setResult(RESULT_OK, data);
        finish();
      }
    }
  }
  
  private void startNewProjectActivity() {
    startActivityForResult(new Intent(this, NewProject.class), Constants.NEW_PROJECT_REQ);
  }
  
  // menü eseménykezelő
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
      case R.id.action_add:
        startNewProjectActivity();
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
