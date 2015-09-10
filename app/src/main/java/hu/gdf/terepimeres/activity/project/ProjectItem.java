package hu.gdf.terepimeres.activity.project;

import android.content.Context;

public interface ProjectItem {
  
  public long getId();
  
  public String getName();
  
  public String getDescrLeft(Context context);
  
  public String getDescrRight(Context context);
}
