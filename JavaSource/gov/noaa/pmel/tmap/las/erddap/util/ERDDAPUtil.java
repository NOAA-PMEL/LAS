package gov.noaa.pmel.tmap.las.erddap.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ERDDAPUtil {
    public static String[] getMinMax(JsonObject bounds, String name) {
        JsonArray rows = (JsonArray) ((JsonObject) (bounds.get("table"))).get("rows");
        JsonArray names = (JsonArray) ((JsonObject) (bounds.get("table"))).get("columnNames");
        int index = -1;
        for (int i = 0; i < names.size(); i++) {
            if ( names.get(i).getAsString().equals(name) ) {
                index = i;
            }
        }
        JsonArray row1 = (JsonArray) rows.get(0);
        JsonArray row2 = (JsonArray) rows.get(1);

        String min = ((JsonElement) row1.get(index)).getAsString();
        String max = ((JsonElement) row2.get(index)).getAsString();
        String[] minmax = new String[2];
        minmax[0] = min;
        minmax[1] = max;
        return minmax;
    }
}
