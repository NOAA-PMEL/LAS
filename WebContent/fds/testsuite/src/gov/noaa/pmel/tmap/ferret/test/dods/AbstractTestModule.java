package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import gov.noaa.pmel.tmap.ferret.test.*;

public abstract class AbstractTestModule 
    extends AbstractModule {

    public abstract void test(String strURL)
        throws Exception;

    protected String addExtensionToURL(String strURL, String extension){
        String dodsPath = strURL;
        if(extension!=null && extension!="") {
            int ceIndex = dodsPath.indexOf('?');
            if(ceIndex>=0){
                dodsPath = dodsPath.substring(0, ceIndex) + "." 
                        + extension + dodsPath.substring(ceIndex);
            }
            else {
                dodsPath = dodsPath + "." + extension;
            }
        }
        return dodsPath;
    }
}

