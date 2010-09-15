package gov.noaa.pmel.tmap.las.client.serializable;

import java.util.ArrayList;
import java.util.List;

public class AnalysisSerializable {
	String label;
    List<AnalysisAxisSerializable> axes = new ArrayList<AnalysisAxisSerializable>();
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public List<AnalysisAxisSerializable> getAxes() {
		return axes;
	}
	public void setAxes(List<AnalysisAxisSerializable> axes) {
		this.axes = axes;
	}

}
