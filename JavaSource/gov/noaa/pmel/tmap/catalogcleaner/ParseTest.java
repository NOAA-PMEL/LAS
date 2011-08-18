package gov.noaa.pmel.tmap.catalogcleaner;

import java.util.Iterator;
import java.util.List;

import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvMetadata;
import thredds.catalog.ThreddsMetadata.Source;

public class ParseTest {
    private static int level = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		InvCatalogFactory factory = new InvCatalogFactory("default", false);
		InvCatalog catalog = (InvCatalog) factory.readXML("http://dunkel.pmel.noaa.gov:8920/thredds/carbon/carbon.xml");
		//InvCatalog catalog = (InvCatalog) factory.readXML("http://oceanwatch.pfeg.noaa.gov/thredds/PaCOOS/GLOBEC/catalog.xml");
		
		StringBuilder buff = new StringBuilder();
		
		if (!catalog.check(buff, false)) {
			System.out.println("Catalog parsing failed: "+buff.toString());
		}
		
		List<InvDataset> datasets = catalog.getDatasets();
		
		for (Iterator<InvDataset> datasetsIt = datasets.iterator(); datasetsIt.hasNext();) {
			InvDataset invDataset = datasetsIt.next();
			writeMetadata(invDataset);
			
		}
	}
    private static void writeMetadata(InvDataset invDataset) {
    	indent(level);	
    	System.out.println(invDataset.getName());
    	List<InvMetadata> metadata = invDataset.getMetadata();
		if ( metadata.size() > 0 ) {
			for (Iterator<InvMetadata> mIt = metadata.iterator(); mIt.hasNext();) {
				InvMetadata invMetadata = mIt.next();
				indent(level+1);
				System.out.println("Metadata: "+invMetadata.toString());
			}
		} else {
			indent(level+1);
			System.out.println("No metadata found in "+invDataset.getName());
		}
	
		List<InvDocumentation> docs = invDataset.getDocumentation();
		if ( docs.size() > 0 ) {
			for (Iterator<InvDocumentation> docsIt = docs.iterator(); docsIt.hasNext();) {
				InvDocumentation invDocumentation =  docsIt.next();
				indent(level+1);
				System.out.println("Documentation: type="+invDocumentation.getType()+" content="+invDocumentation.getInlineContent());
			}
		} else {
			indent(level+1);
			System.out.println("No documentation found in "+invDataset.getName());
		}
		
		List<Source> sources = invDataset.getCreators();
		if ( sources.size() > 0 ) {
			for (Iterator sIt = sources.iterator(); sIt.hasNext();) {
				Source source = (Source) sIt.next();
				indent(level+1);
				System.out.println("Creator: "+source.getName()+" "+source.getEmail()+" "+source.getUrl());
			}
		} else {
			indent(level+1);
			System.out.println("No creators found in "+invDataset.getName());
		}
		level++;
		
		// Write the children...
        List<InvDataset> datasets = invDataset.getDatasets();
		
		for (Iterator<InvDataset> datasetsIt = datasets.iterator(); datasetsIt.hasNext();) {
			InvDataset child = datasetsIt.next();
			writeMetadata(child);			
		}
    }
    private static void indent(int level) {
    	for (int i = 0; i < level; i++) {
			System.out.print("\t");
		}
    }
}
