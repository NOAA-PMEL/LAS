package gov.noaa.pmel.tmap.las.client.serializable;

import java.util.Comparator;

public class VariableNameComparator implements Comparator<VariableSerializable>{

    @Override
    public int compare(VariableSerializable o1, VariableSerializable o2) {
        return o1.getName().compareTo(o2.getName());
    }

}
