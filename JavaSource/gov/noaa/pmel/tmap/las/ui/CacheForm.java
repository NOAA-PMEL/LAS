package gov.noaa.pmel.tmap.las.ui;

import javax.servlet.ServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class CacheForm extends ActionForm {
	private String key;
	private String clean;
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @return the clear
	 */
	public String getClean() {
		return clean;
	}
	/**
	 * @param keys the keys to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @param clear the clear to set
	 */
	public void setClean(String clean) {
		this.clean = clean;
	}
	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.ServletRequest)
	 */
	@Override
	public void reset(ActionMapping mapping, ServletRequest request) {
		super.reset(mapping, request);
		this.clean = null;
		this.key = null;
	}
	

}
