/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.iosp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import thredds.servlet.DatasetSource;
import ucar.nc2.NetcdfFile;
import ucar.unidata.io.RandomAccessFile;

/**
 * This is the implementation of the netCDF Java DataSource interface which allows a class
 * to create a netCDF data source based on the contents of the HTTP Servlet Request.  This
 * is used by F-TDS to make netCDF data source from URLS of the form:
 * http://server.gov/data/file_expr_{data_source, data source2}{command1, command2}{region}
 * @author Roland Schweitzer
 *
 */
public class FerretDataSource implements DatasetSource {
    /* (non-Javadoc)
     * @see thredds.servlet.DatasetSource#getNetcdfFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    static private Logger log = Logger.getLogger(FerretDataSource.class.getName());
    public NetcdfFile getNetcdfFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        StringBuffer jnl = new StringBuffer();
        
        StringBuffer urlbuf = req.getRequestURL();
        String url = URLDecoder.decode(urlbuf.toString(), "UTF-8");

        log.debug("building netcdf file from "+url);
        
        String base = getBaseURL(url);
        ArrayList<String> expressions = getExpressions(url);

        FerretIOServiceProvider fiosp = new FerretIOServiceProvider();
        
        String data_path = fiosp.getDataDir();
        
        jnl.append("use \""+base+"\"\n");
        
        if ( expressions.size() == 2 ) {
            if ( !expressions.get(0).trim().equals("") ) {
                String[] urls = expressions.get(0).split(",");
                for ( int i = 0; i < urls.length; i++ ) {
                    jnl.append("use \""+urls[i]+"\"\n");
                }
            }
            if ( !expressions.get(1).trim().equals("") ) {
                String [] cmds = expressions.get(1).split(";");
                for ( int i = 0; i < cmds.length; i++ ) {
                    jnl.append(cmds[i]+"\n");
                }
            }
        } else if ( expressions.size() == 1 ) {
            if ( !expressions.get(0).trim().equals("") ) {
                String[] urls = expressions.get(0).split(",");
                for ( int i = 0; i < urls.length; i++ ) {
                    jnl.append("use \""+urls[i]+"\"\n");
                }
            }
        }
        
        String key = JDOMUtils.MD5Encode(jnl.toString());
        
        String script = data_path+File.separator+"data_"+key+".jnl";

        log.debug("using "+script+" for temporary script file.");
        
        File script_file = new File(script);
        if (!script_file.exists()) {
           PrintWriter data_script = new PrintWriter(new FileOutputStream(script_file));
           data_script.println(jnl);
           data_script.close();
        }

        RandomAccessFile raf = new RandomAccessFile(script, "r");
       return new FerretNetcdfFile(raf, req.getRequestURI());
    }

    /* (non-Javadoc)
     * @see thredds.servlet.DatasetSource#isMine(javax.servlet.http.HttpServletRequest)
     */
    public boolean isMine(HttpServletRequest req) {
        StringBuffer urlbuf = req.getRequestURL();
        String url;
        try {
            url = URLDecoder.decode(urlbuf.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        if ( url.contains("_expr_") && FerretCommands.containsCommand(url)) {
            return true;
        }
        return false;
    }
    
    private String getBaseURL(String url) {
        return url.substring(0, url.indexOf("_expr_"));
    }
    private ArrayList<String> getExpressions(String url) {
        ArrayList<String> expressions = new ArrayList<String>();
        String[] tokens = null;
        if ( url.contains("_expr_")) {
            url = url.substring(url.indexOf("_expr_")+6, url.length());
            tokens = url.split("\\{");

            for ( int i = 1; i < tokens.length; i++) {
               log.debug("found expression: "+tokens[i].substring(0,tokens[i].indexOf("}")));
                expressions.add(tokens[i].substring(0,tokens[i].indexOf("}")));
            }
        }
        return expressions;
    }

}
