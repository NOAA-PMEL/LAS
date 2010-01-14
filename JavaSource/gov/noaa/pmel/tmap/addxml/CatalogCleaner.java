package gov.noaa.pmel.tmap.addxml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Element;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.InvService;
import thredds.catalog.ServiceType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TypedDatasetFactory;

public class CatalogCleaner {
	
	private static final Logger log = LogManager.getLogger(CatalogCleaner.class);
	private InvCatalogImpl sourceCatalog;
	private InvCatalogImpl cleanCatalog;
	private String key;
	private InvService remoteService;
	private InvService localService;
	
	public CatalogCleaner (InvCatalog catalog) throws URISyntaxException, UnsupportedEncodingException {
		
		sourceCatalog = (InvCatalogImpl) catalog;
		key = JDOMUtils.MD5Encode(catalog.getUriString());
		cleanCatalog = new InvCatalogImpl("Clean Catalog for "+sourceCatalog.getUriString(), "1.0.1", new URI(catalog.getUriString()));
		localService = new InvService("localOPeNDAP_"+key, "OPeNDAP", "/thredds/dodsC/", null, null);
		cleanCatalog.addService(localService);
	}
	public InvCatalogImpl cleanCatalog() throws Exception {
		
		List<InvDataset> threddsDatasets = sourceCatalog.getDatasets();
		for (Iterator dsIt = threddsDatasets.iterator(); dsIt.hasNext();) {
			InvDataset invDataset = (InvDataset) dsIt.next();
			if ( invDataset.hasAccess() ) {
				if ( hasGrid(invDataset) ) {	
					addGridDataset(invDataset);
				}
			}
			if ( invDataset.hasNestedDatasets() ) {
			   clean(invDataset);		
			}
		}
		cleanCatalog.finish();
		return cleanCatalog;
	}
	private void addGridDataset(InvDataset invDataset) {
		InvAccess access = invDataset.getAccess(ServiceType.OPENDAP);
		String url = access.getUrlPath();
        if ( remoteService == null ) {
        	String full_url = access.getStandardUri().toString();
    		String base = full_url.substring(0, full_url.indexOf(url));
        	setService(base);	
        }
		InvDatasetImpl dataset = new InvDatasetImpl((InvDatasetImpl)invDataset);
		dataset.setServiceName(remoteService.getName());
		dataset.setUrlPath(url);
		dataset.setID(dataset.getUrlPath()+"_"+key);
		dataset.setDataType(FeatureType.GRID);
		dataset.finish();
		cleanCatalog.addDataset(dataset);
	}
	private void setService(String base) {		
    	remoteService = new InvService("remoteOPeNDAP_"+key, "OPeNDAP", base, null, null);
    	cleanCatalog.addService(remoteService);
	}
	private void addAggregation(InvDatasetImpl parent, InvDataset invDataset, List<GridDataset> agg, int index) throws Exception {
		InvDatasetImpl aggDatasetNode = new InvDatasetImpl((InvDatasetImpl)invDataset);
		aggDatasetNode.setName(aggDatasetNode.getName()+" "+index);
		aggDatasetNode.setUrlPath("aggregation_"+index);
		aggDatasetNode.setID(aggDatasetNode.getUrlPath()+"_"+key+"_"+index);
		Element ncml = NCML.getRootElement();
		NCML.addAggregationElement(ncml, agg.get(0));
		for (Iterator fileIt = agg.iterator(); fileIt.hasNext();) {
			GridDataset aggDataset = (GridDataset) fileIt.next();
			NCML.addDataset(ncml, aggDataset);
		}
		aggDatasetNode.setServiceName(localService.getName());
		aggDatasetNode.setNcmlElement(ncml);
		aggDatasetNode.setDataType(FeatureType.GRID);
		aggDatasetNode.finish();
		parent.addDataset(aggDatasetNode);
	}
	public void clean(InvDataset invDataset) throws Exception {	
		if ( invDataset.hasNestedDatasets() ) {
			Aggregates aggregates = new Aggregates(invDataset);
			if ( remoteService == null ) {
				setService(aggregates.getBase());
			}
			if ( aggregates.needsAggregation() ) {
				List<List<GridDataset>> aggregations = aggregates.getAggregations();
				InvDatasetImpl parent = new InvDatasetImpl((InvDatasetImpl) invDataset);
				cleanCatalog.addDataset(parent);
				for (int i = 0; i < aggregations.size(); i++) {
					List<GridDataset> agg = (List<GridDataset>) aggregations.get(i);
					addAggregation(parent, invDataset, agg, i);
				}
			} else {
				for (Iterator ndsIt = aggregates.getIndividuals().iterator(); ndsIt.hasNext();) {
					InvDataset nestedDataset = (InvDataset) ndsIt.next();

					if ( nestedDataset.hasAccess() ) { 
						if ( hasGrid(nestedDataset) ) {
							addGridDataset(nestedDataset);
						}
					}
					if ( nestedDataset.hasNestedDatasets() ) {
						clean(nestedDataset);
					}
				}
			}
		} 
	}
	
	private static boolean hasGrid(InvDataset dataset) {
		Boolean has_good_grid = false;
		InvAccess access = dataset.getAccess(ServiceType.OPENDAP);
		
		if ( access != null ) {
			String accessUrl = access.getStandardUrlName();
			try {
				NetcdfDataset nc = NetcdfDataset.openDataset(accessUrl);
				StringBuilder error = new StringBuilder();
				GridDataset gridDataset = (GridDataset) TypedDatasetFactory.open(FeatureType.GRID, nc, null, error);
				if ( gridDataset != null ) {
					
				    has_good_grid = hasGrid(gridDataset);
				    
				}
			} catch (IOException e) {
				log.error("Failed to open "+accessUrl+" with "+e.getLocalizedMessage());
			}
		}
		return has_good_grid;
	}
	public static boolean hasGrid(GridDataset gridDataset) {
		boolean has_good_grid = false;
		List<GridDatatype> grids = gridDataset.getGrids();
	    if ( grids != null && grids.size() > 0 ) {
	    	for (Iterator gridIt = grids.iterator(); gridIt.hasNext();) {
				GridDatatype grid = (GridDatatype) gridIt.next();
				GridCoordSystem gcs = grid.getCoordinateSystem();
				CoordinateAxis1D x = (CoordinateAxis1D) gcs.getXHorizAxis();
				CoordinateAxis1D y = (CoordinateAxis1D) gcs.getYHorizAxis();		
				if ( x.getSize() > 1 && y.getSize() > 1 ) {
	    	        return true;
				}
	    	}
	    }
	    return has_good_grid;
	}
}
