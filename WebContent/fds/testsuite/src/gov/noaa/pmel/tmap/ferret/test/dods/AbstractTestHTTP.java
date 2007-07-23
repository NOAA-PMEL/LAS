package gov.noaa.pmel.tmap.ferret.test.dods;

import java.lang.*;
import java.io.*;
import java.util.*;

import java.net.*;
import dods.dap.*;
import java.util.zip.InflaterInputStream;

import gov.noaa.pmel.tmap.ferret.test.*;

public abstract class AbstractTestHTTP
    extends AbstractTestModule {
    
    public void test(String strURL) 
        throws Exception {
        if(strURL==null)
            return;
 
        String dodsPath = addExtensionToURL(strURL, getModuleID());

        InputStream is;
        boolean accept_deflate = true;

        URL url = new URL(dodsPath);
        is = openConnection(url, accept_deflate);

        int buffSize = 0;

        buffSize = is.available();
        if(buffSize >0){

            FDSTest fdstest = FDSTest.getInstance();

            byte [] buff = new byte[buffSize];
            String outputFileName = FDSUtils.shortenName(dodsPath);
            File outputFile = fdstest.getStore().get(outputFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }
            PrintStream ps = new PrintStream(new FileOutputStream(outputFile));

            int readSize = is.read(buff, 0, buffSize);
            while(readSize>0) {
                String output = new String(buff, 0, readSize);
                ps.println(output);
                readSize = is.read(buff, 0, buffSize);
            }
            ps.flush();
            ps.close();
        
            Task currentTask = Task.currentTask();
            currentTask.getStatus().passLevel(TaskStatus.CONNECTION_LEVEL);
            if(outputFile.exists())
                currentTask.getResultFiles().add(outputFileName);

            boolean passed =
                fdstest.getComparator().compare(dodsPath, exactCompare);

            if(passed) {
                currentTask.getStatus().passLevel(TaskStatus.MAX_LEVEL);
            }
        }
    }

    /**
     * Open a connection to the DODS server.
   * @param url the URL to open.
   * @return the opened <code>InputStream</code>.
   * @exception IOException if an IO exception occurred.
   * @exception DODSException if the DODS server returned an error.
   */
  private InputStream openConnection(URL url, boolean acceptDeflate) throws IOException, DODSException {
    URLConnection connection = url.openConnection();
    if (acceptDeflate)
      connection.setRequestProperty("Accept-Encoding", "deflate");
    connection.connect();

    // theory is that some errors happen "naturally" (under heavy loads i think)
    // so try it 3 times
    InputStream is = null;
    int retry = 1;
    long backoff = 100L;
    while (true) {
      try {
        is = connection.getInputStream(); // get the HTTP InputStream
        break;
        /* if (is.available() > 0)
          break;
        System.out.println("DConnect available==0; retry open ("+retry+") "+url);
        try { Thread.currentThread().sleep(backoff); }
        catch (InterruptedException ie) {} */

      } catch (NullPointerException e) {
        System.out.println("DConnect NullPointer; retry open ("+retry+") "+url);
        try { Thread.currentThread().sleep(backoff); }
        catch (InterruptedException ie) {}

      } catch (FileNotFoundException e) {
        System.out.println("DConnect FileNotFound; retry open ("+retry+") "+url);
        try { Thread.currentThread().sleep(backoff); }
        catch (InterruptedException ie) {}
      }

      if (retry == 3)
        throw new DODSException("Connection cannot be opened");
      retry++;
      backoff *= 2;
    }

    // check headers
    String type = connection.getHeaderField("content-description");
    // System.err.println("Content Description: " + type);
    handleContentDesc(is, type);

    String encoding = connection.getContentEncoding();
    //System.err.println("Content Encoding: " + encoding);
    return handleContentEncoding(is, encoding);
  }

 /**
   * This code handles the Content-Description: header for
   * <code>openConnection</code> and <code>parseMime</code>.
   * Throws a <code>DODSException</code> if the type is
   * <code>dods_error</code>.
   *
   * @param is the InputStream to read.
   * @param type the Content-Description header, or null.
   * @exception IOException if any error reading from the server.
   * @exception DODSException if the server returned an error.
   */
  private void handleContentDesc(InputStream is, String type)
       throws IOException, DODSException {
    if (type != null && type.equals("dods_error")) {
      // create server exception object
      DODSException ds = new DODSException();
      // parse the Error object from stream and throw it
      ds.parse(is);
      throw ds;
    }
  }

  /**
   * This code handles the Content-type: header for
   * <code>openConnection</code> and <code>parseMime</code>
   * @param is the InputStream to read.
   * @param encoding the Content-type header, or null.
   * @return the new InputStream, after applying an
   *    <code>InflaterInputStream</code> filter if necessary.
   */
  private InputStream handleContentEncoding(InputStream is, String encoding) {
    if (encoding != null && encoding.equals("deflate")) {
      return new InflaterInputStream(is);
    } else {
      return is;
    }
  }

  protected boolean exactCompare;
}

