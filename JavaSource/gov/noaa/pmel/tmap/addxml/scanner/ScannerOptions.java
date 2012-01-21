package gov.noaa.pmel.tmap.addxml.scanner;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ScannerOptions extends Options {
	
	public ScannerOptions() {
		super();
		Option scan   = OptionBuilder.withArgName( "url" )
		                                .hasArg()
		                                .withDescription("The URL of the THREDDS catalog to scan.  Only catalogRef elements will be scanned." )
		                                .create( "scan" );
		this.addOption(scan);
	}

}
