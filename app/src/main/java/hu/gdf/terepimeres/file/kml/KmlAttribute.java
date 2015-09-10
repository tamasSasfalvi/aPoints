package hu.gdf.terepimeres.file.kml;

public enum KmlAttribute {
	XMLNS("xmlns"),
	ID("id"),
	NAME("name"),
	SRC("src"),
	WIDTH("width"),
	HEIGHT("height")
	;
	
	public final String	val;
	
	private KmlAttribute(String val) {
		this.val = val;
	}
}
