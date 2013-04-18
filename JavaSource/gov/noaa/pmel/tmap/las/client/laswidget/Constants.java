package gov.noaa.pmel.tmap.las.client.laswidget;
/**
 * A bunch of constants to keep from making typos when referring to certain Strings.
 * @author rhs
 *
 */
public class Constants {
    public static final String DEFAULT_CATID = "default_catid";
	public static final String DEFAULT_DSID = "default_dsid";
	public static final String DEFAULT_VARID = "default_varid";
	public static final String DEFAULT_OP = "default_operation";
	public static final String DEFAULT_OPTION = "default_option";
	public static final String DEFAULT_VIEW = "default_view";
	// Not used at this time.  Probably need default hi and low for all axes...
	public static final String DEFAULT_TIME = "default_time";
	public static final String DEFAULT_Z = "default_z";
	
	// Switch to determine the final container type used by the output panel.
	public static final String FRAME = "frame";
	public static final String IMAGE = "image";
	
	public static final String AUTH_FRAME_ID = "__esg_authenticateFrame";
	
	public static final String NO_MIN_MAX = "No min and max available.";
    public static final String NO_MIN_MAX_MESSAGE = "Min and max were not returned from the server.  Try again after the next plot.";
	
    public static final String DEFAULT_SCALER_OP = "Plot_2D_XY_zoom";
    public static final String DEFAULT_VECTOR_OP = "Plot_vector";
    
    public static final String PROFILE_ESGF = "LAS-ESGF";
    public static final double CONTROLS_WIDTH = 280.;
    
    public static String PICK = "Choose variable from the group above,";
    public static String APPEAR = "then a value from the list that appears here.";
    public static String LOADING = "Loading values from server...";
    
    public static String UPDATE_NEEDED = "APPLY_NEEDED";
}
