/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/
package au.org.tpac.wms.lib;

import org.w3c.dom.*;

import java.util.Vector;

/**
 * This class contains common elements required for parsing and reconstructing XML relating to WMS
 * @author Pauline Mak (pauline@insight4.com, Insight4 Pty. Ltd.)
 */
abstract public class WMSParser
{
    /**
     * Name of this element
     */
    protected String elementName;

    /**
     * Name of element currently being parsed
     */
    protected String xmlElementName;

    /**
     * A list of children elements
     */
    protected Vector innerElements;

    /**
     * Value of this element
     */
    protected String elementValue;

    /**
     * Attributes of this element.  Element parameter is consisted of a string array of length 2.
     * The first elenent is the name of the attribute and the second element is the value.
     */
    protected Vector params;

    /**
     * Attribute namespace.
     */
    protected String attributeNS = null;

    /**
     * Stores any attributes (if any) of the current element (attributes are extra
     * parameters within the element/node <>)
     * @param attributeName name of the attribute
     * @param attributeValue value of the attribute
     */
    abstract protected void saveAttributeData(String attributeName, String attributeValue);

    /**
     * Saves any children node data
     * @param nodeName name of the node
     * @param nodValue value of the node
     */
    abstract protected void saveNodeData(String nodeName, String nodValue);

    /**
     * Processes this node's children
     * @param nodeName name of the child node
     * @param childNode child node to process
     * @return true if it has been processed, false otherwise
     */
    abstract protected boolean processChildren(String nodeName, Node childNode);

    /**
     * Adds itself to the document which has been passed in
     * @param doc document to add this element to
     * @return an new element
     */
    public Element createElement(Document doc)
    {
        setXmlElements(doc);

        Element thisElement = null;

        if(xmlElementName != null)
        {
            thisElement = doc.createElement(this.xmlElementName);

            if(params.size() > 0)
            {
                for(int i = 0; i < params.size(); i++)
                {
                    String[] aParam = (String[])(params.elementAt(i));
                    if(attributeNS != null)
                    {
                        //this sets attribute name spaces!!!
                        thisElement.setAttributeNS(attributeNS, aParam[0], aParam[1]);
                        thisElement.toString();
                    }
                    else
                    {
                        thisElement.setAttribute(aParam[0], aParam[1]);
                    }
                }
            }

            if(innerElements.size() > 0)
            {
                Element anElement = null;
                for(int i = 0; i < innerElements.size(); i++)
                {
                    anElement = (Element)(innerElements.elementAt(i));
                    if(anElement != null)
                    {
                        thisElement.appendChild(anElement);
                    }
                }
            }
        }

        if((elementValue != null) && (thisElement != null))
        {
            Text txtNode = doc.createTextNode(elementValue);
            thisElement.appendChild(txtNode);
        }
        //if this doesn't have a top level element, then return a list of subelements?

        return thisElement;
    }


    /**
     * Resets all XML related elements and calls children specific setXMLElementSelf()
     * method.
     * @param doc document to add elements to
     */
    protected void setXmlElements(Document doc)
    {
        xmlElementName = null;
        this.innerElements = new Vector();
        this.params = new Vector();
        setXMLElementSelf(doc);
    }

    /**
     * Sets the various attributes/node name for populating an XML document
     * with nodes and values
     * @param doc to create nodes/elements from
     */
    abstract protected void setXMLElementSelf(Document doc);

    /**
     * Parses a node which ignores comments and empty nodes
     * @param node node to parse
     */
    public void parse(Node node)
    {
        switch(node.getNodeType())
        {
            case Node.ELEMENT_NODE:
                elementName = node.getNodeName();
                NodeList children = node.getChildNodes();

                //let's look at attributes
                NamedNodeMap map = node.getAttributes();
                for(int p = 0; p < map.getLength(); p++)
                {
                    Attr attribute = (Attr)(map.item(p));

                    saveAttributeData(attribute.getNodeName(), attribute.getNodeValue());
                }

                for(int j = 0; j < children.getLength(); j++)
                {
                    Node childNode = children.item(j);

                    if(childNode.getNodeName() != null)
                    {
                        if(!childNode.getNodeName().equalsIgnoreCase("#comment"))
                        {
                            if(!processChildren(childNode.getNodeName(), childNode))
                            {
                                parse(childNode);
                            }
                        }
                    }

                }
                break;
            case Node.TEXT_NODE:
                if(!node.getNodeValue().trim().equalsIgnoreCase(""))
                {
                    if(!(node.getNodeName().trim().equalsIgnoreCase("#comment")))
                    {
                        saveNodeData(elementName, node.getNodeValue());
                    }
                }
                break;
         }
    }
}
