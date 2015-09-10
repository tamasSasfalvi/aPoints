package hu.gdf.terepimeres.activity;

import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils {
  
  /**
   * megvizsgálja, hogy az adott action kielégítésére van e alkalmazás a készülékre telepítve
   * 
   * @param context
   * @param action
   * @return true ha van legalább 1, egyébként false
   */
  public static boolean isIntentAvailable(android.content.Context context,
                                          String action) {
    final PackageManager packageManager = context.getPackageManager();
    final Intent intent = new Intent(action);
    List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                                                                  PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }
  
  /**
   * Átméretez egy driveon szereplő képet és Bitmap formában adja vissza. Érdemes az imageview méretét is jól
   * meggondolni, az újabb átméretezés elkerülése érdekében.
   * 
   * @param path a kép elérési útja
   * @param size a kép oldalhossza pixelben (2 hatványa legyen a sebesség kedvéért)
   * @return Az átméretezett Bitmap
   */
  public static Bitmap getBitmap(String path,
                                 final int size) {
    
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, o);
    
    // Méretarány meghatározása. Teljesítmény szempontból 2 hatványait szeretjük
    int width_tmp = o.outWidth;
    int height_tmp = o.outHeight;
    int scale = 1;
    while (width_tmp / 2 > size && height_tmp / 2 > size) {
      width_tmp /= 2;
      height_tmp /= 2;
      scale *= 2;
    }
    
    // Mintavételezés
    BitmapFactory.Options o2 = new BitmapFactory.Options();
    o2.inSampleSize = scale;
    return BitmapFactory.decodeFile(path, o2);
  }
}
