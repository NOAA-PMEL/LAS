package gov.noaa.pmel.tmap.las.client.serializable;


import com.google.gwt.user.client.rpc.IsSerializable;

public class GridSerializable implements IsSerializable {
	String ID;
	String points;
	String intervals;
	AxisSerializable xAxis;
	AxisSerializable yAxis;
	AxisSerializable zAxis;
	TimeAxisSerializable tAxis;
	EnsembleAxisSerializable eAxis;
	public String getPoints() {
		return points;
	}
	public void setPoints(String points) {
		this.points = points;
	}
	public void setIntervals(String intervals) {
		this.intervals = intervals;
	}
	public String getIntervals() {
		return intervals;
	}
	/**
	 * @return the xAxis
	 */
	public AxisSerializable getXAxis() {
		return xAxis;
	}
	/**
	 * @return the yAxis
	 */
	public AxisSerializable getYAxis() {
		return yAxis;
	}
	/**
	 * @return the zAxis
	 */
	public AxisSerializable getZAxis() {
		return zAxis;
	}
	/**
	 * @return the tAxis
	 */
	public TimeAxisSerializable getTAxis() {
		return tAxis;
	}
	public EnsembleAxisSerializable getEAxis() {
		return eAxis;
	}
	/**
	 * @param axis the xAxis to set
	 */
	public void setXAxis(AxisSerializable axis) {
		xAxis = axis;
	}
	/**
	 * @param axis the yAxis to set
	 */
	public void setYAxis(AxisSerializable axis) {
		yAxis = axis;
	}
	/**
	 * @param axis the zAxis to set
	 */
	public void setZAxis(AxisSerializable axis) {
		zAxis = axis;
	}
	/**
	 * @param axis the tAxis to set
	 */
	public void setTAxis(TimeAxisSerializable axis) {
		tAxis = axis;
	}
	public void setEAxis(EnsembleAxisSerializable axis) {
		eAxis = axis;
	}
	/**
	 * @return the id
	 */
	public String getID() {
		return ID;
	}
	/**
	 * @param id the id to set
	 */
	public void setID(String id) {
		this.ID = id;
	}
	
	public AxisSerializable getAxis(String type) {
		if ( type.equals("t") ) {
			return tAxis;
		} else if ( type.equals("z") ) {
			return zAxis;
		} else if ( type.equals("y") ) {
			return yAxis;
		} else if ( type.equals("x") ) {
			return xAxis;
		} else if ( type.equals("e") ) {
			return eAxis;
		} else {
			return null;
		}
	}
	public boolean hasT() {
		return getTAxis() != null;
	}
	public boolean hasZ() {
		return getZAxis() != null;
	}
	public boolean hasY() {
		return getYAxis() != null;
	}
	public boolean hasX() {
		return getXAxis() != null;
	}
	public boolean hasE() {
		return getEAxis() != null;
	}
}
