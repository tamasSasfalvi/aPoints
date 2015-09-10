package hu.gdf.terepimeres.entity;

import hu.gdf.terepimeres.Constants;
import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.activity.project.ProjectItem;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import android.content.Context;

public class Gcp implements ProjectItem {
  
  private static final DecimalFormat df   = new DecimalFormat("#.##########");
  
  // az objektum létrehozásának időpontja a konvencionális id
  // kizárójag ez alapján hasonlítunk!!!
  private final long                 id;
  private String                     name = "";
  private String                     time;
  private double                     accuracy;
  private Set<File>                  imgs = new HashSet<File>();
  private Location                   location;
  
  public Gcp(final long id) {
    this.id = id;
  }
  
  public Location getLocation() {
    return location;
  }
  
  public void setLocation(Location location) {
    this.location = location;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Set<File> getImgs() {
    return imgs;
  }
  
  public boolean addImg(File img) {
    return this.imgs.add(img);
  }
  
  public String getTime() {
    return time;
  }
  
  public void setTime(String time) {
    this.time = time;
  }
  
  public double getAccuracy() {
    return accuracy;
  }
  
  public void setAccuracy(double accuracy) {
    this.accuracy = accuracy;
  }
  
  public void setCoords(String coords) {
    if ( coords != null ) {
      StringTokenizer stz = new StringTokenizer(coords, Constants.KML_COORDS_DELIMITER);
      location = new Location(Double.parseDouble(stz.nextToken()),
                              Double.parseDouble(stz.nextToken()),
                              Double.parseDouble(stz.nextToken()));
    }
  }
  
  public long getId() {
    return id;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if ( this == obj )
      return true;
    if ( obj == null )
      return false;
    if ( getClass() != obj.getClass() )
      return false;
    Gcp other = (Gcp) obj;
    if ( id != other.id )
      return false;
    return true;
  }
  
  @Override
  public String getDescrLeft(Context context) {
    return context.getString(R.string.longitude)
                  .concat(": ".concat(Constants.LINE_SEPARATOR.concat(context.getString(R.string.latitude)
                                                                             .concat(": "))));
  }
  
  @Override
  public String getDescrRight(Context context) {
    return df.format(getLocation().getLon())
             .concat(Constants.LINE_SEPARATOR.concat(df.format(getLocation().getLat())));
  }
}
