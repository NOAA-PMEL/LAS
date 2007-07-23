package gov.noaa.pmel.tmap.las.util;

import org.jdom.Element;

public class View extends Container implements ViewInterface {
    public View(Element element) {
        super(element);
    }
    public String getValue() {
        String value = element.getAttributeValue("value");
        if ( value != null ) {
            return value;
        } else {
            return "";
        }
    }
}

