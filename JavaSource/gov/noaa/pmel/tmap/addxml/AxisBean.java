package gov.noaa.pmel.tmap.addxml;

import org.jdom.*;

/**
 * <p>Title: addXML</p>
 *
 * <p>Description: Reads local or OPeNDAP netCDF files and generates LAS XML
 * configuration information.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: NOAA/PMEL/TMAP</p>
 *
 * @author RHS
 * @version 1.0
 */
public class AxisBean extends LasBean {
	private String type;
	private String units;
	private String display_lo;
	private String display_hi;
	private String ddefault;
	private String[] v;
	private boolean modulo;
	private ArangeBean arange;

	public AxisBean() {
	}

	public boolean isModulo() {
		return modulo;
	}

	public void setModulo(boolean modulo) {
		this.modulo = modulo;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public void setDisplay_lo(String display_lo) {
		this.display_lo = display_lo;
	}

	public void setDisplay_hi(String display_hi) {
		this.display_hi = display_hi;
	}

	public void setDdefault(String ddefault) {
		this.ddefault = ddefault;
	}

	public void setV(String[] v) {
		this.v = v;
	}

	public void setArange(ArangeBean arange) {
		this.arange = arange;
	}

	public String getType() {
		return type;
	}

	public String getUnits() {
		return units;
	}

	public String getDisplay_lo() {
		return display_lo;
	}

	public String getDisplay_hi() {
		return display_hi;
	}

	public String getDdefault() {
		return ddefault;
	}

	public String[] getV() {
		return v;
	}

	public ArangeBean getArange() {
		return arange;
	}

	public Element toXml() {
		Element axis = new Element(this.getElement());
		if ( this.type == null ) {
			System.err.println("Axis has no identifying type.  Setting to 'q'.  Check output.");
			this.type = "q";
		}
		axis.setAttribute("type", this.type);
		if ( this.units == null ) {
			System.err.println("Axis has no units.  Setting units to 'none'.  Check output.");
			this.units = "none";
		}
		if ( isModulo() ) {
			axis.setAttribute("modulo", "true");
		}
		axis.setAttribute("units", this.units);
		if (arange != null) {
			Element arangeElement = new Element("arange");
			if ( this.arange.getStart() == null ) {
				System.err.println("Axis has no start value.  Setting to -999999.0.  Check output.");
				this.arange.setStart("-999999.0");
			}
			arangeElement.setAttribute("start", this.arange.getStart());
			if ( this.arange.getSize() == null ) {
				System.err.println("Axis has no size value.  Setting to -999999.0.  Check output.");
				this.arange.setSize("-999999.0");
			}
			arangeElement.setAttribute("size", this.arange.getSize());
			if ( this.arange.getStep() == null ) {
				System.err.println("Axis has no step value.  Setting to -999999.0.  Check output.");
				this.arange.setStep("-999999.0");
			}
			arangeElement.setAttribute("step", this.arange.getStep());
			axis.addContent(arangeElement);
		} else {
			for (int vit = 0; vit < v.length; vit++) {
				Element vElement = new Element("v");
				vElement.addContent(String.valueOf(v[vit]));
				axis.addContent(vElement);
			}
		}
		return axis;
	}

	public String toString() {
		String s = "type=" + type + " units=" + units;
		if (display_lo != null && display_lo != "") {
			s = s + " display_lo=" + display_lo;
		}
		if (display_lo != null && display_lo != "") {
			s = s + " display_lo=" + display_lo;
		}
		if (ddefault != null && ddefault != "") {
			s = s + " ddefault=" + ddefault;
		}
		if (arange != null) {
			s = s + " " + arange.toString();
		}
		if ( v != null ) {
			s = s+" with "+v.length+" points.";
		}
		return s;
	}

	@Override
	public boolean equals(LasBean bean) {
		AxisBean b = null;
		if ( !(bean instanceof AxisBean) ) {
			return false;
		} else {
			b = (AxisBean) bean;
		}
		if ( !type.equals(b.getType() )) return false;
		if ( !units.equals(b.getUnits() )) return false;
		if ( !display_lo.equals(b.getDisplay_lo() )) return false;
		if ( !display_hi.equals(b.getDisplay_hi() )) return false;
		if ( !ddefault.equals(b.getDdefault()) ) return false;
		if ( v != null ) {
			if ( b.getV() == null ) return false;
			if ( !v.equals(b.getV())) return false;
		}
		if ( arange != null ) {
			if ( b.getArange() != null ) return false;
			if ( !arange.equals(b.getArange() )) return false;
		}
		return true;
	}
}
