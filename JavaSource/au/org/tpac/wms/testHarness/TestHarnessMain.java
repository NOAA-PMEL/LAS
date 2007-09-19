/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.testHarness;


import au.org.tpac.wms.lib.*;
import au.org.tpac.wms.lib.version_1_3_0.WMSLayer_130;
import au.org.tpac.wms.request.*;

import java.util.Vector;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * Do all your testing here :)
 */
public class TestHarnessMain
{
    public static void main(String[] args)
    {
        WMSCapabilities_111 cap = new WMSCapabilities_111("http://linux.dev.insight4.com/las/wms_servlet");
        System.out.println(cap.writeCapabilities());


    }
}
