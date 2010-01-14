package gov.noaa.pmel.tmap.addxml;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;

public class GridDatasetComparator implements Comparator<GridDataset> {

	@Override
	public int compare(GridDataset gd1, GridDataset gd2) {
		long diff = gd1.getEndDate().getTime() - gd2.getEndDate().getTime();
		 if ( diff > 0 ) {
			 return 1;
		 } else if ( diff == 0 ) {
			 return 0 ;
		 } else {
			 return -1;
		 }
	}

}
