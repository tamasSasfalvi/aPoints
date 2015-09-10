package hu.gdf.terepimeres.file.kml;

public class Constants {
	
	public static final String	KML_FILE_EXTENSION	= ".kml";
	
	static final String HTML_PLACEMARK_DESCR_STYLE = "<style>"+
            "div {"+
                "display: table-cell;"+
                "vertical-align: middle;"+
            "}"+
            "img {"+
                "max-width: 300px;"+
                "max-height: 300px;"+
            "}"+
            "ul.images {"+
                "margin: 0;"+
                "padding: 0;"+
                "white-space: nowrap;"+
                "overflow-x: auto;"+
                "padding-top: 4px;"+
                "max-width: 300px;"+
            "}"+
            "ul.images li {"+ 
                "display: inline;"+
            "}"+
            "ul.images img {"+
                "max-width: 60px;"+
                "max-height: 60px;"+
            "}"+
        "</style>";
	
	static final String HML_PLACEMARK_DESCR_SCRIPT = "<script>"+
            "function myFunction(img)"+
            "{"+
                "document.getElementById(\"bigImg\").innerHTML = \"<img src='\" + img + \"'/>\";"+
            "}"+
        "</script>";
	
	static final String HTML_PLACEMARK_ACCURACY_TAG_ID = "accVal";
	static final String HTML_PLACEMARK_RECEIVED_TAG_ID = "recVal";
	
	static final String	       HTML_NEW_LINE	     = "<br/>";
}
