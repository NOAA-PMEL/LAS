package gov.noaa.pmel.tmap.addxml.scanner;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ScannerOptions extends Options {
	
	public ScannerOptions() {
		super();
		Option scan          = OptionBuilder.withArgName( "url" )
		                                    .hasArg()
		                                    .withDescription("The URL of the THREDDS catalog to scan.  Only catalogRef elements will be scanned." )
		                                    .create( "scan" );
		Option catalogRegex = OptionBuilder.withArgName("regular_expression")
		                                   .hasArg()
		                                   .withDescription("Only parse catalogRefs whose URL matches this regular expression")
		                                   .create("regex");
		this.addOption(scan);
		this.addOption(catalogRegex);
	}

}
