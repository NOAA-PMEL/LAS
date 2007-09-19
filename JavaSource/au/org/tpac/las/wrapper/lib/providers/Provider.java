/**
 * This software module was contributed by Tasmanian Partnership for
 * Advanced Computing (TPAC) and Insight4 Pty. Ltd. to the Live
 * Access Server project at the US the National Oceanic and Atmospheric
 * Administration (NOAA)in as-is condition. The LAS software is
 * provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that neither NOAA nor TPAC and
 * Insight4 Pty. Ltd. assume liability for any errors contained in
 * the code.  Although this software is released without conditions
 * or restrictions in its use, it is expected that appropriate credit
 * be given to its authors, to TPAC and Insight4 Pty. Ltd. and to NOAA
 * should the software be included by the recipient as an element in
 * other product development.
 **/

package au.org.tpac.las.wrapper.lib.providers;

import au.org.tpac.wms.lib.WMSLayer;
import au.org.tpac.wms.request.WMSRequest;

import java.awt.*;

/**
 * This is the base provider that all converters should be based on.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
abstract public class Provider
{
    /**
     * Get all top level WMS layers
     * @param layerNames
     * @return
     */
    abstract public WMSLayer[] getLayerInfo(String[] layerNames);

    /**
     * get all names of top level WMS layers
     * @return
     */
    abstract public String[] getTopLevelLayerNames();

    /**
     * Create an image based on a WMS Request
     * @param request request for an image
     * @return image
     */
    abstract public Image getImage(WMSRequest request);
}
