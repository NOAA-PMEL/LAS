package gov.noaa.pmel.tmap.addxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import thredds.catalog.InvAccess;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.TypedDatasetFactory;

public class Aggregates {

	List<InvDataset> individualDatasets = new ArrayList<InvDataset>();
	List<List<GridDataset>> aggregations = new ArrayList<List<GridDataset>>();
	String base = null;
	private static final Logger log = LogManager.getLogger(Aggregates.class);
	public Aggregates(InvDataset dataset) {
		if ( dataset.hasNestedDatasets() ) {
			
			List<InvDataset> nestedDatasets = dataset.getDatasets();
			List<GridDataset> gridDatasets = new ArrayList<GridDataset>();
			for (Iterator ndsIt = nestedDatasets.iterator(); ndsIt.hasNext();) {
				InvDataset invDataset = (InvDataset) ndsIt.next();
				if ( invDataset.hasNestedDatasets() ) {
					individualDatasets.add(invDataset);
				} else {
					if ( invDataset.hasAccess() ) {
						InvAccess opendap = invDataset.getAccess(ServiceType.OPENDAP);
						if ( base == null ) {
							String url = opendap.getUrlPath();
							String full_url = opendap.getStandardUri().toString();
				    		base = full_url.substring(0, full_url.indexOf(url));
						}
						if ( opendap != null ) {
							try {
								NetcdfDataset ncds = NetcdfDataset.openDataset(opendap.getStandardUrlName());
								StringBuilder error = new StringBuilder();
								GridDataset gds = (GridDataset) TypedDatasetFactory.open(FeatureType.GRID, ncds, null, error);
								if ( CatalogCleaner.hasGrid(gds) ) {
									gridDatasets.add(gds);
								}
							} catch (IOException e) {
								log.debug("Failed to open: "+opendap.getStandardUrlName());
							}
						}
					}
				}
			}
			if ( gridDatasets.size() > 0 ) {
				GridDataset gridDataset = gridDatasets.get(0);
				List<List<GridDataset>> datasetGroups = new ArrayList<List<GridDataset>>();
				List group0 = new ArrayList<GridDataset>();
				group0.add(gridDataset);
				datasetGroups.add(group0);
				for (int i = 1; i < gridDatasets.size(); i++ ) {
					GridDataset nextGridDataset = gridDatasets.get(i);
					group(nextGridDataset, datasetGroups);
				}
				
				for (Iterator dsgIt = datasetGroups.iterator(); dsgIt.hasNext();) {
					List<GridDataset> group = (List<GridDataset>) dsgIt.next();
					Collections.sort(group, new GridDatasetComparator());
					long end_time = group.get(0).getEndDate().getTime();
					boolean mono = true;
					for (int i = 1; i < group.size(); i++ ) {
						GridDataset gds = (GridDataset) group.get(i);
						if (gds.getEndDate().getTime() > end_time ) {
							end_time = gds.getEndDate().getTime();
						} else { 
							mono = false;
						}
					}
					if ( mono ) {
						aggregations.add(group);
					}
				}
			}
		}  	
	}
    private void group(GridDataset next, List<List<GridDataset>> groups) {
    	boolean added = false;
    	for (Iterator groupIt = groups.iterator(); groupIt.hasNext();) {
			List<GridDataset> group = (List<GridDataset>) groupIt.next();
			GridDataset grid = group.get(0);
			if ( sameGroup(next, grid) ) {
				group.add(next);
				added = true;
			}
		}
    	if ( !added ) {
    		List<GridDataset> group = new ArrayList<GridDataset>();
    		group.add(next);
    		groups.add(group);
    	}
    }
    private boolean sameGroup(GridDataset ds1, GridDataset ds2) {
    	
    	if ( ds1.getGridsets().size() != ds2.getGridsets().size() ) return false;
    	if ( ds1.getDataVariables().size() != ds2.getDataVariables().size() ) return false;
    	List<GridDatatype> variables  = ds1.getGrids();
    	for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
			GridDatatype d_var1 = (GridDatatype) varIt.next();
			
			// Find the same variable in the second data set.
			
			GridDatatype d_var2 = ds2.findGridDatatype(d_var1.getName());
			
			if ( d_var2 == null ) {
				return false;
			} else {
				if ( !checkAxes(d_var1, d_var2) ) return false;
			}			
		}
    	return true;
    }
    private boolean checkAxes(GridDatatype d1, GridDatatype d2) {
    	
    	CoordinateAxis axis1;
    	CoordinateAxis axis2;

    	CoordinateAxis1D axis1_1d;
    	CoordinateAxis1D axis2_1d;

    	// Start with x
    	axis1 = d1.getCoordinateSystem().getXHorizAxis();
    	if ( axis1 instanceof CoordinateAxis1D ) {
    		axis1_1d = (CoordinateAxis1D) axis1;
    	} else {
    		return false;
    	}
    	axis2 = d2.getCoordinateSystem().getXHorizAxis();
    	if (axis2 instanceof CoordinateAxis1D ) {
    		axis2_1d = (CoordinateAxis1D) axis2;
    	} else {
    		return false;
    	}
    	if ( !axis1_1d.equals(axis2_1d)) {
    		return false;
    	}
    	
    	// Do y
    	axis1 = d1.getCoordinateSystem().getYHorizAxis();
    	if ( axis1 instanceof CoordinateAxis1D ) {
    		axis1_1d = (CoordinateAxis1D) axis1;
    	} else {
    		return false;
    	}
    	axis2 = d2.getCoordinateSystem().getYHorizAxis();
    	if (axis2 instanceof CoordinateAxis1D ) {
    		axis2_1d = (CoordinateAxis1D) axis2;
    	} else {
    		return false;
    	}
    	if ( !axis1_1d.equals(axis2_1d)) {
    		return false;
    	}
    	
    	// Do z
    	axis1 = d1.getCoordinateSystem().getVerticalAxis();
    	axis2 = d2.getCoordinateSystem().getVerticalAxis();
    	if ( axis1 != null && axis2 != null ) {
    		if ( axis1 instanceof CoordinateAxis1D ) {
    			axis1_1d = (CoordinateAxis1D) axis1;
    		} else {
    			return false;
    		}

    		if (axis2 instanceof CoordinateAxis1D ) {
    			axis2_1d = (CoordinateAxis1D) axis2;
    		} else {
    			return false;
    		}
    		if ( !axis1_1d.equals(axis2_1d)) {
    			return false;
    		}
    	}
    	return true;
    }
    public List<List<GridDataset>> getAggregations() {
    	return aggregations;
    }
    public List<InvDataset> getIndividuals() {
    	return individualDatasets;
    }
    public boolean needsAggregation() {
    	return aggregations.size() > 0;
    }
    public boolean hasIndividualDataset() {
    	return individualDatasets.size() > 0;
    }
    public String getBase() {
    	return base;
    }
}
