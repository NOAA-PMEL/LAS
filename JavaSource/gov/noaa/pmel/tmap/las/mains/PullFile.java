package gov.noaa.pmel.tmap.las.mains;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import gov.noaa.pmel.tmap.las.ui.LASProxy;

public class PullFile {

    /**
     * @param args
     */
    public static void main(String[] args) {
        LASProxy lasProxy = new LASProxy();
        if (args.length <= 0 ) {
            System.out.println("Add URL to grab to arguments.");
        }
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

        for (int i = 0; i < args.length; i++) {
            String dsUrl = args[i];
            
            try {
               
                if (dsUrl.startsWith("'") ) {
                    dsUrl = dsUrl.substring(1);
                }
                if ( dsUrl.endsWith("'") ) {
                    dsUrl = dsUrl.substring(0, dsUrl.length()-1);
                }
                File out = new File("out"+String.valueOf(i));
                DateTime dt = new DateTime();
                System.out.println("TableDapTool starting file pull for the only file at "+fmt.print(dt));
                lasProxy.executeGetMethodAndSaveResult(dsUrl, out, null);
                dt = new DateTime();
                System.out.println("TableDapTool finishing file pull for the only file at "+fmt.print(dt));
            } catch (HttpException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
