package gov.noaa.pmel.tmap.las.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AxisSerializable extends Serializable implements IsSerializable {
	
	String ID;
	String type;
	String hi;
	String lo;
	ArangeSerializable arange;
	Map<String, String> v;
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @return the hi
	 */
	public String getHi() {
		return hi;
	}
	/**
	 * @return the lo
	 */
	public String getLo() {
		return lo;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @param hi the hi to set
	 */
	public void setHi(String hi) {
		this.hi = hi;
	}
	/**
	 * @param lo the lo to set
	 */
	public void setLo(String lo) {
		this.lo = lo;
	}
	/**
	 * @param v the v to set
	 */
	public void setV(Map<String, String> v) {
		this.v = v;
	}
	/**
	 * @return the id
	 */
	public String getID() {
		return ID;
	}
	/**
	 * @return the v
	 */
	public Map<String, String> getV() {
		return v;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.ID = id;
	}
	
	public ArangeSerializable getArangeSerializable() {
		return arange;
	}
	public void setArangeSerializable(ArangeSerializable arange) {
		this.arange = arange;
	}
}
