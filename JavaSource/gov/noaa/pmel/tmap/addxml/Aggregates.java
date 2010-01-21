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

	List<DatasetGridPair> individualDatasets = new ArrayList<DatasetGridPair>();
	List<List<DatasetGridPair>> aggregations = new ArrayList<List<DatasetGridPair>>();
	String base = null;
	boolean aggregate;
	boolean done = false;
	public Aggregates(List<InvDataset> nestedDatasets, boolean aggregate) {
		this.aggregate = aggregate;
		
		List<List<DatasetGridPair>> datasetGroups = new ArrayList<List<DatasetGridPair>>();
		List<DatasetGridPair> group0 = new ArrayList<DatasetGridPair>();
		
		int index = 0;
		for (Iterator ndsIt = nestedDatasets.iterator(); ndsIt.hasNext();) {
			InvDataset invDataset = (InvDataset) ndsIt.next();
            // We know this already :-)
			if ( invDataset.hasAccess() ) {
				InvAccess opendap = invDataset.getAccess(ServiceType.OPENDAP);
				
				if ( opendap != null ) {
					if ( base == null ) {
						String url = opendap.getUrlPath();
						String full_url = opendap.getStandardUri().toString();
						base = full_url.substring(0, full_url.indexOf(url));
					}
					try {
						NetcdfDataset ncds = NetcdfDataset.openDataset(opendap.getStandardUrlName());
						StringBuilder error = new StringBuilder();
						GridDataset gds = (GridDataset) TypedDatasetFactory.open(FeatureType.GRID, ncds, null, error);
						if ( CatalogCleaner.hasGrid(gds) ) {
							
							if ( index == 0 ) {
								group0.add(new DatasetGridPair(invDataset, gds));
								datasetGroups.add(group0);
							} else {
								group(new DatasetGridPair(invDataset, gds), datasetGroups);
								if ( done ) {
									return;
								}
							}
							index++;
						}
						
					} catch (IOException e) {
						Cleaner.error("AGGREGATES: Failed to open: "+opendap.getStandardUrlName(), 2);
					}
				}
			}

		}
		Cleaner.info("AGGREGATES: Grids extracted", 2);

		// Move single data sets to the individual list
		List<Integer> singles = new ArrayList<Integer>();
		for (int i = 0; i < datasetGroups.size(); i++) {
			List<DatasetGridPair> group = (List<DatasetGridPair>) datasetGroups.get(i);
			if ( group.size() == 1 ) {
				singles.add(i);
			}
		}
		for ( int i = 0; i < singles.size(); i++ ) {
			List<DatasetGridPair> group = (List<DatasetGridPair>) datasetGroups.get(singles.get(i));
			individualDatasets.add(group.get(0));
			datasetGroups.remove(singles.get(i));
		}
		Cleaner.info("AGGREGATES: Grids groupped", 2);
		// sort the rest
		for (Iterator dsgIt = datasetGroups.iterator(); dsgIt.hasNext();) {
			List<DatasetGridPair> group = (List<DatasetGridPair>) dsgIt.next();
			Collections.sort(group, new GridDatasetComparator());
			long end_time = group.get(0).getGrid().getEndDate().getTime();
			boolean mono = true;
			for (int i = 1; i < group.size(); i++ ) {
				GridDataset gds = (GridDataset) group.get(i).getGrid();
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
    private void group(DatasetGridPair next, List<List<DatasetGridPair>> groups) {
    	boolean added = false;
    	for (Iterator groupIt = groups.iterator(); groupIt.hasNext();) {
			List<DatasetGridPair> group = (List<DatasetGridPair>) groupIt.next();
			GridDataset grid = group.get(0).getGrid();
			if ( sameGroup(next.getGrid(), grid) ) {
				group.add(next);
				if ( !aggregate && group.size() > 3 ) {
					aggregations.add(group);
					done = true;
				}
				added = true;
			}
		}
    	if ( !added ) {
    		List<DatasetGridPair> group = new ArrayList<DatasetGridPair>();
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
    public List<List<DatasetGridPair>> getAggregations() {
    	return aggregations;
    }
    public List<DatasetGridPair> getIndividuals() {
    	return individualDatasets;
    }
    public boolean needsAggregation() {
    	// Only need aggregation if one list has at least two members
    	for (Iterator aggIt = aggregations.iterator(); aggIt.hasNext();) {
			List<DatasetGridPair> group = (List<DatasetGridPair>) aggIt.next();
			if ( group.size() > 1 ) {
				return true;
			}
		}
    	return false;
    }
    public boolean hasIndividualDataset() {
    	return individualDatasets.size() > 0;
    }
    public String getBase() {
    	return base;
    }
}
