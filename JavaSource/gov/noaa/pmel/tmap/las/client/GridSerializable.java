package gov.noaa.pmel.tmap.las.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GridSerializable implements IsSerializable {
	String ID;
	AxisSerializable xAxis;
	AxisSerializable yAxis;
	AxisSerializable zAxis;
	AxisSerializable tAxis;
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
	public AxisSerializable getTAxis() {
		return tAxis;
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
	public void setTAxis(AxisSerializable axis) {
		tAxis = axis;
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
}
