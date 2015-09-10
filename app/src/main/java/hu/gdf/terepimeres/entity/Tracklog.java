package hu.gdf.terepimeres.entity;

import hu.gdf.terepimeres.activity.project.ProjectItem;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class Tracklog implements ProjectItem {
  
  private final long     id;
  private String         name;
  private List<Location> locations = new ArrayList<Location>();
  
  public Tracklog(final long id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public long getId() {
    return id;
  }
  
  @Override
  public String getDescrLeft(Context context) {
    return "";
  }
  
  @Override
  public String getDescrRight(Context context) {
    return "";
  }
  
  public List<Location> getLocations() {
    return locations;
  }
  
  public void addLocation(Location loc) {
    locations.add(loc);
  }
  
  public Location getLastLoc() {
    return locations.get(locations.size() - 1);
  }
}
