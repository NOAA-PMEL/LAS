package gov.noaa.pmel.tmap.addxml;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ErddapScannerOptions extends Options {
    
    public ErddapScannerOptions() {
        Option url = new Option("u", "url", true, "The base URL of the tabledap section of the ERDDAP server (ends in \"/tabledap\"");
        url.setRequired(true);
        addOption(url);
        
        Option id = new Option("i", "id", true, "The ERDDAP ID of the trajectory data set (see the \"/tabledap\" page on the server.");
        addOption(id);
        
        Option axes = new Option("a", "axes", true, "By default, program will attempt to download the range of an axis. Use this option list which axes (xyzt) to skip.");
        addOption(axes);
    }

}
