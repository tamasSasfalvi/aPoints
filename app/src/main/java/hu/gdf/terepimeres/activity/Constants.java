package hu.gdf.terepimeres.activity;


public class Constants {
	
	// ACTIVITY REQUESTS
	public static final int	   NEW_PROJECT_REQ	= 1;
	public static final int	   OPEN_PROJECT_REQ	= 2;
	public static final int	   EDIT_GCP_REQ	    = 3;
	public static final int	   GET_PICTURE	    = 4;
	
	// ACTIVITY RESULTS
	// a startActivityForResult metódusok által használható id-k
	public static final String	PROJECT_NAME	  = "pName";
	public static final String	LONGITUDE	      = "lon";
	public static final String	LATITUDE	      = "lat";
	public static final String	GCP_ID	        = "gcpName";
	
}
