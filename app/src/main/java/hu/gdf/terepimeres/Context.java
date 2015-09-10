package hu.gdf.terepimeres;

import hu.gdf.terepimeres.file.Utils;
import hu.gdf.terepimeres.file.kml.KML;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Context {
  
  public static final boolean DEVELOPER_MODE = false;
  
  public static KML           kml;
  public static String        projectName;
  public static File          workspace;
  
  private static Locale       locale;
  private static DateFormat   shortDateTimeFormat;
  private static DateFormat   shortDateFormat;
  
  private static File         tempDir;
  private static File         tempPic;
  
  public static Locale getLocale() {
    return locale;
  }
  
  public static void setLocale(Locale locale) {
    Context.locale = locale;
    shortDateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT,
                                                               SimpleDateFormat.SHORT,
                                                               locale);
    shortDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale);
  }
  
  public static DateFormat getShortDateFormat() {
    return shortDateFormat;
  }
  
  public static DateFormat getShortDateTimeFormat() {
    return shortDateTimeFormat;
  }
  
  public static File getProjectDirectory() {
    return new File(workspace, projectName);
  }
  
  public static void clearTempDir() {
    Utils.deleteDir(getTempDir(), false);
  }
  
  public static File getTempDir() {
    if ( tempDir == null || !tempDir.exists() ) {
      tempDir = new File(workspace, hu.gdf.terepimeres.Constants.TEMP_DIR_NAME);
      if ( !tempDir.exists() ) {
        tempDir.mkdir();
      }
    }
    return tempDir;
  }
  
  public static File getTempPic() {
    if ( tempPic == null || !tempPic.exists() ) {
      tempPic = new File(getTempDir(), Constants.TEMP_PIC_NAME);
      try {
        tempPic.createNewFile();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    return tempPic;
  }
  
}
