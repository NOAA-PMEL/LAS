/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import java.util.ArrayList;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class Dataset extends Container implements DatasetInterface {
    public Dataset(Element dataset) {
        super(dataset);
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.DatasetInterface#getXPath()
     */
    public String getXPath() {
        return "/lasdata/datasets/dataset@[ID='"+getID()+"']";
    }
}
