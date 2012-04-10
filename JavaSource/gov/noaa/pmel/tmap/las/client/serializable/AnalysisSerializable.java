package gov.noaa.pmel.tmap.las.client.serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalysisSerializable {
	String label;
	Map<String, AnalysisAxisSerializable> axes = new HashMap<String, AnalysisAxisSerializable>();
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Map<String, AnalysisAxisSerializable> getAxes() {
		return axes;
	}
	public void setAxes(List<AnalysisAxisSerializable> axes) {
		Map<String, AnalysisAxisSerializable> axMap = new HashMap<String, AnalysisAxisSerializable>();
		for (Iterator axIt = axes.iterator(); axIt.hasNext();) {
			AnalysisAxisSerializable axis = (AnalysisAxisSerializable) axIt.next();
			axMap.put(axis.getType(), axis);
		}
		this.axes = axMap;
	}
	public void setAxes(Map<String, AnalysisAxisSerializable> axes) {
		this.axes = axes;
	}
	public boolean isActive(String axis) {
		AnalysisAxisSerializable a = axes.get(axis);
		if ( a == null ) {
			return false;
		} else {
			return a.getOp() != null;
		}		
	}
	public String toString() {
		StringBuilder ts = new StringBuilder();
		for (Iterator keyIt = axes.keySet().iterator(); keyIt.hasNext();) {
			String key = (String) keyIt.next();
			AnalysisAxisSerializable a = axes.get(key);
			if (a.getOp() != null ) {
				ts.append(a.toString());
			}
		}
		return ts.toString();
	}
}
