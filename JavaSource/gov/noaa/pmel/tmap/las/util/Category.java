package gov.noaa.pmel.tmap.las.util;

import java.util.List;

import org.jdom.Element;

public class Category extends Container implements CategoryInterface {
    public Category(Element category) {
        super(category);
    }
    public boolean hasVariableChildren() {
        List variables = element.getChild("variables").getChildren("variables");
        if ( variables != null && variables.size() > 0 ) {
            return true;
        }
        return false;
    }
}
