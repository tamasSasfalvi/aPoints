package hu.gdf.terepimeres.file.kml;

public enum KmlTag {
  KML("kml"),
  DOCUMENT("Document"),
  NAME("name"),
  DESCRIPTION("description"),
  FOLDER("Folder"),
  PLACEMARK("Placemark"),
  POINT("Point"),
  COORDINATES("coordinates"),
  EXTRUDE("extrude"),
  ALTITUDEMODE("altitudeMode"),
  EXTENDEDDATA("ExtendedData"),
  DATA("Data"),
  VALUE("value"),
  HMTL("html"),
  IMG("img"),
  LINESTRING("LineString"),
  STYLE("Style"),
  LINESTYLE("LineStyle"),
  COLOR("color"),
  WIDTH("width"),
  STYLEMAP("StyleMap"),
  PAIR("Pair"),
  KEY("key"),
  STYLEURL("styleUrl"),
  OPEN("open"),
  VISIBILITY("visibility");
  
  public final String val;
  
  private KmlTag(String val) {
    this.val = val;
  }
}
