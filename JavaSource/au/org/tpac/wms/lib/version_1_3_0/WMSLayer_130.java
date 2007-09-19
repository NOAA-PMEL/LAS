package au.org.tpac.wms.lib.version_1_3_0;

import au.org.tpac.wms.lib.*;

import java.util.Vector;
import java.util.Hashtable;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class WMSLayer_130 extends WMSLayer
{
    protected int fixedWidth;
    protected int fixedHeight;
    protected boolean noSubsets;
    protected Vector keywords;
    protected Vector CRS;
    protected WMSExGeoBox_130 exGeoBox;
    protected WMSAttribution_130 attribution;
    protected WMSAuthURL_130 authURL;
    protected WMSIdentifier_130 identifier;
    protected WMSFeatureList_130 features;
    protected WMSResource dataURL;
    protected WMSBoundingBox_130 boundingBox;
    protected WMSMetadata_130 metadata;

    public WMSLayer_130()
    {
        super();
        fixedWidth = 0;
        fixedHeight = 0;
        keywords = new Vector();
        CRS = new Vector();
        authURL = null;
        attribution = null;
        exGeoBox = null;
    }

    public WMSLayer_130(WMSLayer_130 _parent)
    {
        super(_parent);
        fixedWidth = 0;
        fixedHeight = 0;
        keywords = new Vector();
        CRS = new Vector();
        authURL = null;
        attribution = null;
        exGeoBox = null;
    }

    public Vector getCoordSystem()
    {
        return getSupportedCRS();
    }

    /**
     * Get CRS that this layer supports.
     * CRS is the type of projection this layer supports.<br>
     * This property can be inherited
     * @return a Vector of Strings, where each value is the name of the supported CRS.
     */
    public Vector getSupportedCRS()
    {
        Vector parentCRS = null;
        WMSLayer_130 par = (WMSLayer_130)parent;

        if(par != null)
        {
            parentCRS = (par.getSupportedCRS());
        }

        if(CRS != null)
        {
            if(parentCRS != null)
            {
                parentCRS = (Vector)(parentCRS.clone());
                Vector thisClone = (Vector)(this.CRS.clone());

                thisClone.addAll(parentCRS);
                return thisClone;
            }

            else
            {
                return CRS;
            }
        }

        return parentCRS;
    }

    public int getFixedWidth()
    {
        return fixedWidth;
    }

    public int getFixedHeight()
    {
        return fixedHeight;
    }

    public Vector getKeywords()
    {
        return keywords;
    }

    public Vector getCRS()
    {
        //do some stuff here
        return CRS;
    }

    public WMSExGeoBox_130 getGeoBox()
    {
        return exGeoBox;
    }

    public WMSAttribution_130 getAttribution()
    {
        return attribution;
    }



    protected void saveAttributeData(String attName, String attValue)
    {
        super.saveAttributeData(attName, attValue);

        if(attName.equalsIgnoreCase("fixedWidth"))
        {
            this.fixedWidth = Integer.parseInt(attValue);
        }
        else if(attName.equalsIgnoreCase("fixedHeight"))
        {
            this.fixedHeight = Integer.parseInt(attValue);
        }
        else if(attName.equalsIgnoreCase("noSubsets"))
            this.noSubsets = attValue.equalsIgnoreCase("1");


    }

    public boolean getNoSubsets()
    {
        return this.noSubsets;
    }

    protected void saveNodeData(String nodeName, String nodeData)
    {
        super.saveNodeData(nodeName, nodeData);

        if(nodeName.equalsIgnoreCase("keyword"))
        {
            keywords.add(nodeData);
        }
        else if(nodeName.equalsIgnoreCase("CRS"))
        {
            String[] splitCRS = nodeData.split(" ");

            if(splitCRS.length > 0)
            {
                for(int i = 0; i < splitCRS.length; i++)
                {
                    CRS.add(splitCRS[i]);
                }
            }
            else
                CRS.add(nodeData);
        }
     }

    protected WMSAuthURL_130 getAuthURL()
    {
        if(this.authURL == null)
        {
            if(parent != null)
            {
                WMSLayer_130 par = (WMSLayer_130)parent;
                return par.getAuthURL();
            }
        }
        return null;
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        if(childName.equalsIgnoreCase("EX_GeographicBoundingBox"))
        {
            exGeoBox = new WMSExGeoBox_130(childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("layer"))
        {
            WMSLayer_130 myLayer = new WMSLayer_130(this);
            myLayer.parse(childNode);
            if(subLayers == null)
                subLayers = new Vector();
            this.subLayers.add(myLayer);
            return true;
        }
        else if(childName.equalsIgnoreCase("Attribution"))
        {
            attribution = new WMSAttribution_130(childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("style"))
        {
            WMSStyle_130 myStyle = new WMSStyle_130();
            myStyle.parse(childNode);

            if(this.styles == null)
                styles = new Vector();
            styles.add(myStyle);
            return true;
        }
        else if(childName.equalsIgnoreCase("AuthorityURL"))
        {
            authURL = new WMSAuthURL_130(childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("Identifier"))
        {
            if(getAuthURL() != null)
            {
                identifier = new WMSIdentifier_130(childNode, getAuthURL());
                return true;
            }
        }
        else if(childName.equalsIgnoreCase("boundingbox"))
        {
            this.boundingBox = new WMSBoundingBox_130((Element)childNode);
            return true;
        }
        else if(childName.equalsIgnoreCase("FeatureListURL"))
        {
            features = new WMSFeatureList_130(childNode);
        }
        else if(childName.equalsIgnoreCase("DataURL"))
        {
            dataURL = new WMSResource(childNode);
        }
        else if(childName.equalsIgnoreCase("Dimension"))
        {
            WMSDimension_130 extent = new WMSDimension_130();
            extent.parse(childNode);

            if(extents == null)
                extents = new Hashtable();

            extents.put(extent.getExtentName(), extent);
            return true;
        }
        else if(childName.equalsIgnoreCase("MetadataURL"))
        {
            metadata = new WMSMetadata_130(childNode);
            return true;
        }

        return false;
    }

}
