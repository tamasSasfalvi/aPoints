package hu.gdf.terepimeres;

import java.text.DateFormat;

public class Constants {
  
  public static final String     LINE_SEPARATOR       = System.getProperty("line.separator");
  
  public static final String     KML_COORDS_DELIMITER = ",";
  
  // Minden projektkönytárban lennie kell egy ilyen nevű fájlnak
  public static final String     PROJECT_FILE_NAME    = ".project";
  
  // fényképek kiterjesztése
  public static final String     JPEG_EXTENSION       = ".jpg";
  
  public static final DateFormat TIME_FORMAT          = DateFormat.getTimeInstance(DateFormat.FULL);
  
  public static final DateFormat DATE_TIME_FORMAT     = DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                                                       DateFormat.FULL);
  
  // tempkönyvtár neve
  static final String            TEMP_DIR_NAME        = ".temp";
  
  // ideiglenes kép neve
  static String                  TEMP_PIC_NAME        = "pic".concat(JPEG_EXTENSION);
}
