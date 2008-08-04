package gov.noaa.pmel.tmap.las.service.kml;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.Period;

import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;

import java.util.HashMap;
import java.io.IOException;

import org.apache.log4j.Logger;

/*
 * A point that represents an OSMC insitu observation
 * @author Jing Yang Li
 */
public class OSMCInsituPoint extends LASInsituPoint{

    private static Logger log = Logger.getLogger(OSMCInsituPoint.class);

    private String lat;
    private String lon;
    private String time;
    private String country;
    private String platformType;
    private String platformID;

    public OSMCInsituPoint(String lat, String lon){
        super(lat,lon);
    }

    public OSMCInsituPoint(String lat, String lon, String time){
        super(lat,lon, time);
    }

    public void setCountry(String c){
		country = c;
    }

//TODO: need to fix the ferret script, list_grid_osmc.jnl, to list the full name of type
    public void setPlatformType(String pt){
		platformType = pt;

		if(platformType.contains("DRIFTING BUOYS")){
			platformType = "DRIFTING BUOYS (GENERIC)";
		}

		if(platformType.contains("MOORED BUOYS")){
					platformType = "MOORED BUOYS (GENERIC)";
		}

		if(platformType.contains("VOLUNTEER OBSERV")){
					platformType = "VOLUNTEER OBSERVING SHIPS (GENERIC)";
		}

		if(platformType.contains("PROFILING FLOATS")){
					platformType = "PROFILING FLOATS AND GLIDERS";
		}

        if(platformType.contains("UNDERWAY CARBON")){
	        platformType = "UNDERWAY CARBON SHIPS (GENERIC)";
		}

		if(platformType.contains("TIDE GAUGE")){
	        platformType = "TIDE GAUGE STATION";
	    }

	    if(platformType.contains("REAL TIME REPORT")){
			platformType = "REAL TIME REPORTING";
	    }
	}

    public void setPlatformID(String pi){
				platformID = pi;
	}


    public String getCountry(){
		return country;
    }


    public String getPlatformType(){
		return platformType;
    }

    public String getPlatformID(){
			return platformID;
    }

    /**
     * get the icon URL based on country and platform type
     */
    public String getIconURL(String baseURL, String colorBy){
        String iconURL = baseURL.replaceFirst("output","images");

        //color by platform type
        if(colorBy.equalsIgnoreCase("platform")){
			if(platformType.contains("SHIPS") || platformType.contains("VOSCLIM") ||
                    platformType.contains("VOLUNTEER OBSERV")){
		 	    iconURL += "/ship.PNG";
  		    }else if(platformType.contains("DRIFTING BUOYS") || platformType.contains("ICE BUOYS") ||
		         platformType.contains("HURRICANE DRIFTERS")){
			    iconURL += "/drifting_buoys.PNG";
	        }else if(platformType.contains("MOORED") || platformType.contains("TSUNAMI") ||
	             platformType.contains("WEATHER BUOYS")){
			    iconURL += "/moored_buoys.PNG";
	        }else if(platformType.contains("C-MAN") || platformType.contains("TIDE GAUGE") ||
	             platformType.contains("NWLON") || platformType.contains("GLOSS") ||
	             platformType.contains("REAL TIME REPORTING") ){
			    iconURL += "/cman.PNG";
	        }else if(platformType.contains("UNKNOWN")){
			    iconURL += "/unknown.png";
		    }else if(platformType.contains("PROFILING FLOATS")){
				iconURL += "/argo/argo.PNG";
		    }else{
		        iconURL = "http://maps.google.com/mapfiles/kml/pal4/icon29.png";
		    }
        //color by country
	    }else if(colorBy.equalsIgnoreCase("country")){
		    if(platformType.contains("SHIPS") || platformType.contains("VOSCLIM") ||
               platformType.contains("VOLUNTEER OBSERV")){
 			    if(country.equalsIgnoreCase("AU")){
			        iconURL += "/ship/au_ship.png";
			    }else if(country.equalsIgnoreCase("BR")){
			        iconURL += "/ship/br_ship.png";
			    }else if(country.equalsIgnoreCase("CA")){
			        iconURL += "/ship/ca_ship.png";
			    }else if(country.equalsIgnoreCase("EU")){
			        iconURL += "/ship/eu_ship.png";
			    }else if(country.equalsIgnoreCase("DE")){
			        iconURL += "/ship/de_ship.png";
			    }else if(country.equalsIgnoreCase("FR")){
			        iconURL += "/ship/fr_ship.png";
			    }else if(country.equalsIgnoreCase("IN")){
				    iconURL += "/ship/in_ship.png";
			    }else if(country.equalsIgnoreCase("IE")){
				    iconURL += "/ship/ie_ship.png";
			    }else if(country.equalsIgnoreCase("JP")){
				    iconURL += "/ship/jp_ship.png";
			    }else if(country.equalsIgnoreCase("NL")){
			        iconURL += "/ship/nl_ship.png";
			    }else if(country.equalsIgnoreCase("NO")){
				    iconURL += "/ship/no_ship.png";
			    }else if(country.equalsIgnoreCase("NZ")){
			        iconURL += "/ship/nz_ship.png";
			    }else if(country.equalsIgnoreCase("SA")){
				    iconURL += "/ship/sa_ship.png";
			    }else if(country.equalsIgnoreCase("UK")){
			        iconURL += "/ship/uk_ship.png";
			    }else if(country.equalsIgnoreCase("US")){
			        iconURL += "/ship/us_ship.png";
		        }else{
			        iconURL += "/ship/other_ship.png";
			    }
	        }else if(platformType.contains("DRIFTING BUOYS") || platformType.contains("ICE BUOYS") ||
					         platformType.contains("HURRICANE DRIFTERS")){
			    if(country.equalsIgnoreCase("AU")){
					iconURL += "/drifting_buoys/au_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("BR")){
					iconURL += "/drifting_buoys/br_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("CA")){
					iconURL += "/drifting_buoys/ca_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("EU")){
					iconURL += "/drifting_buoys/eu_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("DE")){
					iconURL += "/drifting_buoys/de_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("FR")){
					iconURL += "/drifting_buoys/fr_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("IN")){
					iconURL += "/drifting_buoys/in_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("IE")){
					iconURL += "/drifting_buoys/ie_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("JP")){
					iconURL += "/drifting_buoys/jp_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("NL")){
					iconURL += "/drifting_buoys/nl_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("NO")){
					iconURL += "/drifting_buoys/no_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("NZ")){
					iconURL += "/drifting_buoys/nz_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("SA")){
					iconURL += "/drifting_buoys/sa_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("UK")){
					iconURL += "/drifting_buoys/uk_drifting_buoys.PNG";
				}else if(country.equalsIgnoreCase("US")){
					iconURL += "/drifting_buoys/us_drifting_buoys.PNG";
				}else{
				    iconURL += "/drifting_buoys/other_drifting_buoys.PNG";
			    }
			}else if(platformType.contains("MOORED") || platformType.contains("TSUNAMI") ||
				             platformType.contains("WEATHER BUOYS")){

				if(country.equalsIgnoreCase("AU")){
				    iconURL += "/moored_buoys/au_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("BR")){
				    iconURL += "/moored_buoys/br_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("CA")){
				    iconURL += "/moored_buoys/ca_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("EU")){
				    iconURL += "/moored_buoys/eu_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("DE")){
				    iconURL += "/moored_buoys/de_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("FR")){
				    iconURL += "/moored_buoys/fr_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("IN")){
					iconURL += "/moored_buoys/in_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("IE")){
					iconURL += "/moored_buoys/ie_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("JP")){
					iconURL += "/moored_buoys/jp_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("NL")){
				    iconURL += "/moored_buoys/nl_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("NO")){
					iconURL += "/moored_buoys/no_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("NZ")){
				    iconURL += "/moored_buoys/nz_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("SA")){
					iconURL += "/moored_buoys/sa_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("UK")){
				    iconURL += "/moored_buoys/uk_moored_buoys.PNG";
				}else if(country.equalsIgnoreCase("US")){
				    iconURL += "/moored_buoys/us_moored_buoys.PNG";
				}else{
				    iconURL += "/moored_buoys/other_moored_buoys.PNG";
				}
			}else if(platformType.contains("C-MAN") || platformType.contains("TIDE GAUGE") ||
	             platformType.contains("NWLON") || platformType.contains("GLOSS") ||
	             platformType.contains("REAL TIME REPORTING") ){
				if(country.equalsIgnoreCase("AU")){
				    iconURL += "/cman/au_cman.PNG";
				}else if(country.equalsIgnoreCase("BR")){
				    iconURL += "/cman/br_cman.PNG";
				}else if(country.equalsIgnoreCase("CA")){
				    iconURL += "/cman/ca_cman.PNG";
				}else if(country.equalsIgnoreCase("EU")){
				    iconURL += "/cman/eu_cman.PNG";
				}else if(country.equalsIgnoreCase("DE")){
				    iconURL += "/cman/de_cman.PNG";
				}else if(country.equalsIgnoreCase("FR")){
				    iconURL += "/cman/fr_cman.PNG";
				}else if(country.equalsIgnoreCase("IN")){
					iconURL += "/cman/in_cman.PNG";
				}else if(country.equalsIgnoreCase("IE")){
					iconURL += "/cman/ie_cman.PNG";
				}else if(country.equalsIgnoreCase("JP")){
					iconURL += "/cman/jp_cman.PNG";
				}else if(country.equalsIgnoreCase("NL")){
				    iconURL += "/cman/nl_cman.PNG";
				}else if(country.equalsIgnoreCase("NO")){
					iconURL += "/cman/no_cman.PNG";
				}else if(country.equalsIgnoreCase("NZ")){
				    iconURL += "/cman/nz_cman.PNG";
				}else if(country.equalsIgnoreCase("SA")){
					iconURL += "/cman/sa_cman.PNG";
				}else if(country.equalsIgnoreCase("UK")){
				    iconURL += "/cman/uk_cman.PNG";
				}else if(country.equalsIgnoreCase("US")){
				    iconURL += "/cman/us_cman.PNG";
				}else{
				    iconURL += "/cman/other_cman.PNG";
				}
			}else if(platformType.contains("PROFILING FLOATS")){
			    if(country.equalsIgnoreCase("AU")){
				    iconURL += "/argo/au_argo.PNG";
				}else if(country.equalsIgnoreCase("BR")){
				    iconURL += "/argo/br_argo.PNG";
				}else if(country.equalsIgnoreCase("CA")){
				    iconURL += "/argo/ca_argo.PNG";
				}else if(country.equalsIgnoreCase("EU")){
				    iconURL += "/argo/eu_argo.PNG";
				}else if(country.equalsIgnoreCase("DE")){
				    iconURL += "/argo/de_argo.PNG";
				}else if(country.equalsIgnoreCase("FR")){
				    iconURL += "/argo/fr_argo.PNG";
				}else if(country.equalsIgnoreCase("IN")){
					iconURL += "/argo/in_argo.PNG";
				}else if(country.equalsIgnoreCase("IE")){
					iconURL += "/argo/ie_argo.PNG";
				}else if(country.equalsIgnoreCase("JP")){
					iconURL += "/argo/jp_argo.PNG";
				}else if(country.equalsIgnoreCase("NL")){
				    iconURL += "/argo/nl_argo.PNG";
				}else if(country.equalsIgnoreCase("NO")){
					iconURL += "/argo/no_argo.PNG";
				}else if(country.equalsIgnoreCase("NZ")){
				    iconURL += "/argo/nz_argo.PNG";
				}else if(country.equalsIgnoreCase("SA")){
					iconURL += "/argo/sa_argo.PNG";
				}else if(country.equalsIgnoreCase("UK")){
				    iconURL += "/argo/uk_argo.PNG";
				}else if(country.equalsIgnoreCase("US")){
				    iconURL += "/argo/us_argo.PNG";
				}else{
				    iconURL += "/argo/other_argo.PNG";
				}
			}else if(platformType.contains("UNKNOWN")){
				iconURL += "/unknown/unknown.png";
			}else{
				iconURL = "http://maps.google.com/mapfiles/kml/pal4/icon29.png";
		    }
		}else{
		    iconURL = "http://maps.google.com/mapfiles/kml/pal4/icon29.png";
		}
		return iconURL;
	}
}
