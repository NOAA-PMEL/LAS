package gov.noaa.pmel.tmap.las.jdom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataTable {
    List<String> headers = new ArrayList<String>();
    List<String> preamble = new ArrayList<String>();
    Map<String, List<String>> data = new HashMap<String, List<String>>();
    public DataTable() {
    }
    public DataTable(File file) throws IOException {
       read(file);
    }
   
    public void read(File chart) throws IOException {
        String[] headers;
        Map<String, List<String>> data = new HashMap<String, List<String>>();

        BufferedReader reader = new BufferedReader(new FileReader(chart));
        String line = reader.readLine();
        boolean columnStart = false;
        boolean columnEnd = false;
        boolean headersStart = false;
        boolean dataStart = false;
        String h0 = null;
        int cols = 0;
        while ( line != null ) {
            if ( line.contains("Column ") ) {
                cols++;
                columnStart = true;
            } else {
                if ( columnStart ) {
                    columnEnd = true;
                }
            }

            
            if ( columnStart && columnEnd && !headersStart) {
                headersStart = true;
                headers = line.trim().split("\\s+");
                List<String> h = new ArrayList<String>();
                if ( headers.length == cols -1 ) {
                    h.add("DATE");
                    h0 = "DATE";
                } else {
                    h0 = headers[0];
                }
                for (int i = 0; i < headers.length; i++) {
                    h.add(headers[i]);
                }
                setHeaders(h);
            } else if ( !headersStart && !columnEnd ) {
                preamble.add(line.trim());
            }
            if (headersStart) {
                if ( dataStart && h0 != null ) {
                    String date_string = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                    line = line.substring(line.lastIndexOf("\"")+1, line.length()); 
                    List<String> datelist = data.get(h0);
                    if ( datelist == null ) {
                        datelist = new ArrayList<String>();
                        data.put(h0, datelist);
                    }
                    datelist.add(date_string);
                    String[] values = line.trim().split("\\s+");
                    for (int i = 1; i < getHeaders().size(); i++ ) {
                        String header = getHeaders().get(i);
                        List<String> datalist = data.get(header);
                        if ( datalist == null ) {
                            datalist = new ArrayList<String>();
                            data.put(header, datalist);
                        }
                        datalist.add(values[i-1].trim());
                    }
                }
                dataStart = true;
            }
            line = reader.readLine();
        }
        setData(data);
    }
    public List<String> getPreamble() {
        return preamble;
    }
    public void setPreamble(List<String> preamble) {
        this.preamble = preamble;
    }
    public List<String> getHeaders() {
        return headers;
    }
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
    public Map<String, List<String>> getData() {
        return data;
    }
    public void setData(Map<String, List<String>> data) {
        this.data = data;
    }
}
