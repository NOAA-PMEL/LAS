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

package au.org.tpac.las.wrapper.lib.data;

import au.org.tpac.wms.lib.WMSParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * This contains information about datasets.
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
public class LasDatasetInfo extends WMSParser
{

    /**
     * A human readable name of the dataset
     */
    protected String title;

    /**
     * Variables that this dataset contains.  This is a hashmap where keys
     * are the variable IDs and the values are of type LasVariable
     */
    protected HashMap variables;

    /**
     * Abstract/documentation
     */
    protected String doc;

    /**
     * URL to the data source (can be file of actual URL to a remote file)
     */
    protected String url;

    /**
     * Unique identifier for this dataset
     */
    protected String datasetId;

    /**
     * Grid contains the dimension range (x, y, z and t)
     */
    protected LasGrid grid;    //this is assigned later!
    
    /**
     * Full XML path to this dataset (used by composites to combine different variables/dataset, etc)s
     */
    protected String fullPath;


    /**
     * Empty constructor
     */
    public LasDatasetInfo()
    {
        title = null;
        datasetId = null;
        variables = new HashMap();
    }


    /**
     * This constructor takes in an XML node and parses all values into class variables
     * @param node XMl node to parse
     */
    public LasDatasetInfo(Node node)
    {
        title = null;
        datasetId = null;
        variables = new HashMap();

        parse(node);
    }

     protected void setXMLElementSelf(Document doc)
    {

    }

    /**
     * Retrieves a human readable name for this dataset
     * @return name for this dataset
     */
    public String getTitle()
    {
        return title;
    }


    /**
     * Retrieves documentation related to this dataset
     * @return a URL that points to documentation files
     */
    public String getDoc()
    {
        return doc;
    }

    /**
     * Retrieves this dataset's unique identifier
     * @return ID of this dataset
     */
    public String getDatasetId()
    {
        return datasetId;
    }


    /**
     * Add range information to this dataset
     * @param grid grid to add.  Note that this will check whether a grid belongs to this dataset.
     */
    public void addGrid(LasGrid grid)
    {
        Set set = variables.keySet();
        Iterator it = set.iterator();

        while(it.hasNext())
        {
            String key = (String)(it.next());
            LasVariable var = (LasVariable)(variables.get(key));

            if(var.getLink().equalsIgnoreCase(grid.getName()))
            {
                var.setGrid(grid);
            }
        }
    }

    /**
     * Retrieves variables in this dataset
     * @return variables are stored as a hashmap with variable IDs as the key and LasVariable as values
     */
    public HashMap getVariables()
    {
        return variables;
    }

    /**
     * Retrieves grid information
     * @return a LasGrid that will contain ranges of dimension this dataset uses
     */
    public LasGrid getGrid()
    {
        return grid;
    }

    /**
     * Retrieves a single variable based on the variableID
     * @param varName a unique identifier for this variable
     * @return null if no variable of varName is found, or a LasVariable if one ss found.
     */
    public LasVariable getVariable(String varName)
    {
        if(variables.containsKey(varName))
            return (LasVariable)(variables.get(varName));
        return null;
    }

    protected void saveAttributeData(String attributeName, String attributeValue)
    {
        if(attributeName.equalsIgnoreCase("name"))
        {
            if(title == null)
                title = attributeValue;
        }
        else if(attributeName.equalsIgnoreCase("url"))
        {
            if(url == null)
                url = attributeValue;
        }
        else if(attributeName.equalsIgnoreCase("ID"))
        {
            if(datasetId == null)
                this.datasetId = attributeValue;
        }
    }


    protected void saveNodeData(String nodeName, String nodValue)
    {

    }

    protected boolean processChildren(String nodeName, Node childNode)
    {
        if(childNode.getParentNode().getNodeName().equalsIgnoreCase("variables"))
        {
            if(!nodeName.equalsIgnoreCase("#text"))
            {
                LasVariable var = new LasVariable(childNode);
                variables.put(var.getVarId(), var);
                return true;
            }
        }
        else if(childNode.getParentNode().getNodeName().equalsIgnoreCase("composite"))
        {
            System.err.println("Composites are not digested...");
            return true;
        }
        return false;
    }
}
