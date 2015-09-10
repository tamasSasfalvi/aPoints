package hu.gdf.terepimeres.entity;

public class Location {
  
  private final double lon;
  private final double lat;
  private final double alt;
  
  public Location(final double lon, final double lat, final double alt) {
    this.lon = lon;
    this.lat = lat;
    this.alt = alt;
  }
  
  public Location(android.location.Location location) {
    this(location.getLongitude(), location.getLatitude(), location.getAltitude());
  }
  
  public double getAlt() {
    return alt;
  }
  
  public double getLon() {
    return lon;
  }
  
  public double getLat() {
    return lat;
  }
  
  @Override
  public String toString() {
    return lon + hu.gdf.terepimeres.Constants.KML_COORDS_DELIMITER + lat
        + hu.gdf.terepimeres.Constants.KML_COORDS_DELIMITER + alt;
  }
  
}
