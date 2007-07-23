package gov.noaa.pmel.tmap.ferret.server;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

// JAXP APIs
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.FactoryConfigurationError; 
import javax.xml.parsers.ParserConfigurationException;
 
import org.xml.sax.*; 

// DOM
import org.w3c.dom.*;

// DODS
import dods.dap.*;
import dods.dap.Server.*;

import gov.noaa.pmel.tmap.ferret.server.dodstype.*;

/** A helper class to handle XML output by Ferret server
 * 
 * @author Yonghua Wei, Richard Roger
 */
public class FerretXMLReader {


    /** Generates a Document from a XML input stream
     * @param is the input xml stream
     * @throws Exception if something goes wrong during the parsing process
     */
    public static Document XML2DOM(InputStream is) 
	throws Exception // handled @ a higher level
    {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new MyResolver());
        return builder.parse( is );
    }

    /** Generates a Document from a XML string.
     * @param xml the input xml string
     * @throws Exception if something goes wrong during the parsing process
     */
    public static Document XML2DOM(String xml) 
	throws Exception // handled @ a higher level
    {
        InputStream is = new StringBufferInputStream(xml); 
        return XML2DOM( is );
    }

    /** Generates a Document from a XML file
     * @param file the input xml file handle
     * @throws Exception if something goes wrong during the parsing process
     */
    public static Document XML2DOM(File file) 
	throws Exception // handled @ a higher level
    {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new MyResolver());
        return builder.parse( file );
    }

    /**Converts a Document to a DDS object.<p>
     *
     * @param node the document node used to generate DDS object.
     * @param dds the generated DDS object.
     */
    public static void DOM2DDS(Node node, ServerDDS dds) {
        DOM2DDS(node, dds, null);
    }


    /** Converts a Document to a DDS object with specified variables
     *
     * @param node the document node used to generate DDS object.
     * @param dds the generated DDS object.
     * @param variables the list of variables that should be in the dds.
     *     It is a string of comma-separated or space-separated variables.
     *     If null, all variables should be in the DDS.
     */
    public static void DOM2DDS(Node node, ServerDDS dds, String variables) {

        Map varMap = getVarAxisSet(node, variables);
        Set touchedVars = new HashSet();

	Node dataTag = node.getFirstChild();
	for (Node elemTag = dataTag.getFirstChild(); 
	     elemTag != null;
	     elemTag = elemTag.getNextSibling()) {
    
	   if (elemTag.getNodeType()==1) {
              if (elemTag.getNodeName().equals("axes")){
                   for (Node elemTag1 = elemTag.getFirstChild();
                        elemTag1 != null;
                        elemTag1  = elemTag1.getNextSibling()) {
                        if(elemTag1.getNodeType()==1 
                         && elemTag1.getNodeName().equals("axis")) {
                          String axisName = getElemName(elemTag1);
                           if(varMap.containsKey(axisName)){
 		               FerretArray axis = new FerretArray();
		               // basetype & array name
		               axis.addVariable( new GenericFloat64(axisName) );
		               axis.appendDim(getElemLength(elemTag1), axisName);
		               dds.addVariable(axis);
                           }
                       }
                   }
               }
           }
        }

	for (Node elemTag = dataTag.getFirstChild(); 
	     elemTag != null;
	     elemTag = elemTag.getNextSibling()) {
    
	   if (elemTag.getNodeType()==1) {
               if (elemTag.getNodeName().equals("global")) {
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null;
                       elemTag1  = elemTag1.getNextSibling()) {
	               if (elemTag1.getNodeType()==1
                           && elemTag1.getNodeName().equals("var")) {
                           String varName = getElemName(elemTag1);
                           if(!touchedVars.contains(varName) 
                              && varMap.containsKey(varName)){
                               touchedVars.add(varName);
		               FerretGrid grid = new FerretGrid(varName);
		               FerretArray var = new FerretArray();
		               var.addVariable ( new GenericFloat32(getElemName(elemTag1)));
		               appendDims(elemTag1, dds, var, grid);
		               grid.addVariable(var, DGrid.ARRAY);
		               dds.addVariable(grid);
                           }
                       }   
                  }
	      }
              else if (elemTag.getNodeName().equals("datasets")) {
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null;
                       elemTag1  = elemTag1.getNextSibling()) {
                       if(elemTag1.getNodeType()==1 
                          && elemTag1.getNodeName().equals("dataset")) {
                          for (Node elemTag2 = elemTag1.getFirstChild();
                               elemTag2 != null;
                               elemTag2 = elemTag2.getNextSibling()) {  
	                       if (elemTag2.getNodeType()==1
                                   && elemTag2.getNodeName().equals("var")) {
                                   String varName = getElemName(elemTag2);
                                   if(!touchedVars.contains(varName) 
                                      && varMap.containsKey(varName)){
                                      touchedVars.add(varName);
		                      FerretGrid grid = new FerretGrid(varName);
		                      FerretArray var = new FerretArray();
		                      var.addVariable ( new GenericFloat32(getElemName(elemTag2)));
		                      appendDims(elemTag2, dds, var, grid);
		                      grid.addVariable(var, DGrid.ARRAY);
		                      dds.addVariable(grid);
                                 }
                              }
                          }
                      }
                  }
	      }
	  }
       }
    }

    /** Generates a DDS object from a XML string <code>xml</code> using 
     * <code>datasetName</code> as dataset name.
     * @param datasetName the dataset name of the dataset
     * @param xml the input xml string
     */
    public static ServerDDS XML2DDS(String datasetName, String xml) 
	throws Exception // handled @ a higher level
    {
	FerretTypeFactory baseTypeFactory = new FerretTypeFactory();
        ServerDDS dds = new ServerDDS(datasetName, baseTypeFactory);
        DOM2DDS(XML2DOM(xml), dds);
        return dds;
    }

    /** Generates a DAS object from a xml node
     *
     * @param node the input XML node
     * @param das the output DAS object
     */
    public static void DOM2DAS(Node node, DAS das) {
        DOM2DAS(node, das, null);
    }

    /** Generates a DAS object from a xml node with specified variables
     *
     * @param node the input XML node
     * @param das the output DAS object
     * @param variables the specified variables that should be in the DAS.
     *     It is a string with comma-separated or space-separated variables.
     *     If null, all variables should be in the DAS.
     */
    public static void DOM2DAS(Node node, DAS das, String variables) {

        Map varMap = getVarAxisSet(node, variables);
        Set touchedVars = new HashSet();

	Node dataSetTag = node.getFirstChild();
	for (Node elemTag = dataSetTag.getFirstChild(); 
	     elemTag != null;
	     elemTag = elemTag.getNextSibling()) {
    
	    if (elemTag.getNodeType()==1) {		
                if (elemTag.getNodeName().equals("axes")){
                    for (Node elemTag1 = elemTag.getFirstChild();
                         elemTag1 != null;
                         elemTag1  = elemTag1.getNextSibling()) {
                        if(elemTag1.getNodeType()==1 
                            && elemTag1.getNodeName().equals("axis")) {
                            String axisName = getElemName(elemTag1);
                            if(varMap.containsKey(axisName)) {
                                 AttributeTable attrs = new AttributeTable();
		                 getAttributes(elemTag1, attrs);

                                 //ywei: A temporary fix for y axis being "modulo"
                                 //Remove these line after Ferret fixes this
                                 String axisType = (String)varMap.get(axisName);
                                 if(axisType.equals("yaxis")||axisType.equals("zaxis")){
                                     Attribute moduloAtt = attrs.getAttribute("modulo");
                                     if(moduloAtt!=null 
                                        && moduloAtt.getValueAt(0).equals("\"yes\"")){
                                         try{
                                             moduloAtt.deleteValueAt(0);
                                             moduloAtt.appendValue("\"no\"");
                                         }
                                         catch(AttributeBadValueException abve){}
                                     }
                                 }
                                 //temporary fix ends

		                 das.addAttributeTable(axisName, attrs);
                            }
                        }
                    }                   
                }
            }
        }
	for (Node elemTag = dataSetTag.getFirstChild(); 
	     elemTag != null;
	     elemTag = elemTag.getNextSibling()) {
    
	    if (elemTag.getNodeType()==1) {
                 if (elemTag.getNodeName().equals("global")) {
                     for(Node elemTag1 = elemTag.getFirstChild();
                         elemTag1 != null;
                         elemTag1  = elemTag1.getNextSibling()) {
                         if (elemTag1.getNodeType()==1
                             && elemTag1.getNodeName().equals("var")) {
                             String varName = getElemName(elemTag1);
                             if(!touchedVars.contains(varName)
                                && varMap.containsKey(varName)) {
                                 touchedVars.add(varName);
                                 AttributeTable attrs = new AttributeTable();
	                         getAttributes(elemTag1, attrs);
	                         das.addAttributeTable(varName, attrs);
                             }
                         }
                     }
	         }
                 else if (elemTag.getNodeName().equals("datasets")) {
                    for (Node elemTag1 = elemTag.getFirstChild();
                         elemTag1 != null;
                         elemTag1  = elemTag1.getNextSibling()) {
                        if(elemTag1.getNodeType()==1 
                            && elemTag1.getNodeName().equals("dataset")) {
                            for (Node elemTag2 = elemTag1.getFirstChild();
                                 elemTag2 != null;
                                 elemTag2 = elemTag2.getNextSibling()) {  
	                        if (elemTag2.getNodeType()==1
                                    && elemTag2.getNodeName().equals("var")) {
                                    String varName = getElemName(elemTag2);
                                    if(!touchedVars.contains(varName)
                                       && varMap.containsKey(varName)) {
                                        touchedVars.add(varName);
                                        AttributeTable attrs = new AttributeTable();
		                        getAttributes(elemTag2, attrs);
		                        das.addAttributeTable(varName, attrs);
                                    }
                                }
                            }
                        }
                    }
	        }
	    }
        }
    }
    
    /** Gets a list of variables that are in a given xml node
     *
     * @param node the input xml node
     * @return a list of variable names in String type
     */
    public static List getVariables(Node node) {

        List returnVal = new ArrayList();
  
	Node dataTag = node.getFirstChild();
	for(Node elemTag = dataTag.getFirstChild(); 
	    elemTag != null;
	    elemTag = elemTag.getNextSibling()) {
    
	    if(elemTag.getNodeType()==1) {
                if(elemTag.getNodeName().equals("datasets")) {
                    for (Node elemTag1 = elemTag.getFirstChild();
                         elemTag1 != null;
                         elemTag1  = elemTag1.getNextSibling()) {
                         if(elemTag1.getNodeType()==1 
                            && elemTag1.getNodeName().equals("dataset")) {
                            for (Node elemTag2 = elemTag1.getFirstChild();
                                 elemTag2 != null;
                                 elemTag2 = elemTag2.getNextSibling()) {  
	                         if (elemTag2.getNodeType()==1
                                     && elemTag2.getNodeName().equals("var")) {
                                     String varName = getElemName(elemTag2);
                                     returnVal.add(varName);
                                 }
                            }
                        }
                    }
	        }
	    }
        }
        return returnVal;
    }

   /** Returns i for X axis, j for Y, k for Z, l for T
    *  @param dom the xml document for the data
    *  @param varName the variable name
    *  @param dimName the dimension name
    *  @return string "i" for X axis, "j" for Y axis, "k"
    *      for Z axis and "l" for T axis
    */
    public static String whichDim (Node dom, String varName, String dimName) 
	throws Exception
    {
       // search for <var name="varName">
       Node varNode = null;

       Node dataTag = dom.getFirstChild();
       boolean foundVar = false;
       for (Node elemTag = dataTag.getFirstChild(); 
	     elemTag != null && !foundVar;
	     elemTag = elemTag.getNextSibling()) {
    
	  if (elemTag.getNodeType()==1) {
              if (elemTag.getNodeName().equals("global")) {
                   for (varNode = elemTag.getFirstChild();
                        varNode != null;
                        varNode = varNode.getNextSibling()) {  
	                if (varNode.getNodeType()==1
                            && varNode.getNodeName().equals("var")) {
                            if (varName.equals(getElemName(varNode))){
                                foundVar = true;
                                break;
			    }
		        }
		   }  
	      }
              else if (elemTag.getNodeName().equals("datasets")) {
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null && !foundVar;
                       elemTag1  = elemTag1.getNextSibling()) {
                       if(elemTag1.getNodeType()==1 
                         && elemTag1.getNodeName().equals("dataset")) {
                          for (varNode = elemTag1.getFirstChild();
                               varNode != null;
                               varNode = varNode.getNextSibling()) {  
	                      if (varNode.getNodeType()==1
                                  && varNode.getNodeName().equals("var")) {
                                  if (varName.equals(getElemName(varNode))){
                                      foundVar = true;
                                      break;
				  }
			      }
			  }
		       }
		  }
	      }
	  }
       }

       // if varNode == null something very bad has happened
       if (varNode == null) throw new Exception ("variable " +
		 varName + " not found");

       // now find the child of varNode that contains the axes
       Node gridNode;
       for (gridNode = varNode.getFirstChild();
	    gridNode != null && !gridNode.getNodeName().equals("grid");
	    gridNode = gridNode.getNextSibling()) {}

       // again, things are horribly broken if gridNode == null
       if (gridNode == null) throw new Exception ("variable " +
		 varName + " has no grid");

       // find the child of gridNode whose text matches dimName
       Node axesNode=null;
       for(axesNode = gridNode.getFirstChild();
           axesNode != null;
           axesNode = axesNode.getNextSibling())
       {
           if (axesNode.getNodeType() != 1) continue;
           if (axesNode.getNodeName().equals("axes")) break;
       }
       if(axesNode == null) throw new Exception ("variable " +
		varName + " has no \"axes\" node.");

       Node axisNode=null;
       for (axisNode = axesNode.getFirstChild();
	    axisNode != null; 
	    axisNode = axisNode.getNextSibling())
       {
	   if (axisNode.getNodeType() != 1) continue;
	   if (dimName.equals(encodeName(axisNode.getFirstChild().getNodeValue())))
	       break;
       }
       // if axisNode doesn't have one child or == null reality is broken
       if (axisNode == null) throw new Exception ("variable " +
		varName + " has no " + dimName + " axis");

       // so now axisNode is the dimension we are looking for. Now we
       // need to know if it's x, y, z, or t

       if (axisNode.getNodeName().equals("xaxis")) return "i";
       else if (axisNode.getNodeName().equals("yaxis")) return "j";
       else if (axisNode.getNodeName().equals("zaxis")) return "k";
       else if (axisNode.getNodeName().equals("taxis")) return "l";
       // if it wasn't x,y,z, or t, the world has ended
       throw new Exception (varName + "'s " + dimName + 
	     " axis does not appear to be x, y, z, or t");
    }

   /** Returns the index of the dataset the variable varName is in
    *  @param dom the xml document that describes the dataset
    *  @param varName the specified variable name
    *  @return the index of the dataset which the specifed variabe 
    *      belongs to
    */
    public static int whichDset (Node dom, String varName) 
	throws Exception
    {
       // search for <var name="varName">
       int returnVal = -1;
       int datasetIndex = -1;
       Node varNode = null;

       Node dataTag = dom.getFirstChild();
       boolean foundVar = false;
       for (Node elemTag = dataTag.getFirstChild(); 
	     elemTag != null && !foundVar;
	     elemTag = elemTag.getNextSibling()) {
    
	  if (elemTag.getNodeType()==1) {
              if (elemTag.getNodeName().equals("global")) {
                  datasetIndex++;
                  for (varNode = elemTag.getFirstChild();
                       varNode != null;
                       varNode = varNode.getNextSibling()) {  
	                if (varNode.getNodeType()==1
                            && varNode.getNodeName().equals("var")) {
                            if (varName.equals(getElemName(varNode))){
                                 foundVar = true;
                                 returnVal = datasetIndex;
                                 break;
		            }
		        }
		   }
	      }
              else if (elemTag.getNodeName().equals("datasets")) {
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null && !foundVar;
                       elemTag1  = elemTag1.getNextSibling()) {
                       if(elemTag1.getNodeType()==1 
                         && elemTag1.getNodeName().equals("dataset")) {
                          datasetIndex++;
                          for (varNode = elemTag1.getFirstChild();
                               varNode != null;
                               varNode = varNode.getNextSibling()) {  
	                      if (varNode.getNodeType()==1
                                  && varNode.getNodeName().equals("var")) {
                                  if (varName.equals(getElemName(varNode))){
                                      foundVar = true;
                                      returnVal = datasetIndex;
                                      break;
				  }
			      }
			  }
		       }
		  }
	      }
	  }
       }
       return returnVal;
    }
   /** Returns the index of the dataset the variable varName is in
    *  @param dom the xml document that describes the dataset
    *  @param varName the specified variable name
    *  @return the index of the dataset which the specifed variabe 
    *      belongs to
    */
    public static boolean hasDerivedAxes (Node dom, String varName) 
	throws Exception
    {
       // search for <var name="varName">
       boolean returnVal = false;
       Node varNode = null;

       Node dataTag = dom.getFirstChild();
       boolean foundVar = false;
       for (Node elemTag = dataTag.getFirstChild(); 
	     elemTag != null && !foundVar;
	     elemTag = elemTag.getNextSibling()) {
    
	  if (elemTag.getNodeType()==1) {
              if (elemTag.getNodeName().equals("global")) {
                   for (varNode = elemTag.getFirstChild();
                       varNode != null;
                       varNode = varNode.getNextSibling()) {  
	                if (varNode.getNodeType()==1
                            && varNode.getNodeName().equals("var")) {
                            if (varName.equals(getElemName(varNode))){
                                 foundVar = true;
                                 for(Node gridTag = varNode.getFirstChild();
                                     gridTag != null;
                                     gridTag = gridTag.getNextSibling()) {
                                     if(gridTag.getNodeType()==1
                                        && gridTag.getNodeName().equals("grid")) {
                                          String gridName = decodeName(getElemName(gridTag));
                                          if(gridName.startsWith("(")&&gridName.endsWith(")")){
                                              returnVal = true;
                                          }
                                     }
                                 }
                                 break;
		             }
		        }
		   }
	      }
              else if (elemTag.getNodeName().equals("datasets")) {
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null && !foundVar;
                       elemTag1  = elemTag1.getNextSibling()) {
                       if(elemTag1.getNodeType()==1 
                         && elemTag1.getNodeName().equals("dataset")) {
                          for (varNode = elemTag1.getFirstChild();
                               varNode != null;
                               varNode = varNode.getNextSibling()) {  
	                      if (varNode.getNodeType()==1
                                  && varNode.getNodeName().equals("var")) {
                                  if (varName.equals(getElemName(varNode))){
                                      foundVar = true;
                                      for(Node gridTag = varNode.getFirstChild();
                                         gridTag != null;
                                         gridTag = gridTag.getNextSibling()) {
                                         if(gridTag.getNodeType()==1
                                            && gridTag.getNodeName().equals("grid")) {
                                             String gridName = decodeName(getElemName(gridTag));
                                             if(gridName.startsWith("(")&&gridName.endsWith(")")){
                                                 returnVal = true;
                                             }
                                         }
                                      }
                                      break;
				  }
			      }
			  }
		       }
		  }
	      }
	  }
       }
       return returnVal;
    }
    /** This is a helper class that allows xml parsing to skip those xml files
     *  that can not be found.
     */
    private static class MyResolver implements EntityResolver {
        // This method is called whenever an external entity is accessed
        // for the first time.

        public InputSource resolveEntity (String publicId, String systemId) {
            try {
                // Wrap the systemId in a URI object to make it convenient
                // to extract the components of the systemId
                URI uri = new URI(systemId);
    
                // Check if external source is a file
                if ("file".equals(uri.getScheme())) {
                    String filename = uri.getSchemeSpecificPart();
                    File file = new File(filename);
                    if(!file.exists()){
                       return new InputSource(new StringReader(""));
                    }
                    else{
                       return new InputSource(new FileReader(filename));
		    }
                }
            } catch (URISyntaxException e) {
            } catch (IOException e) {}
    
            // Returning null causes the caller to try accessing the systemid
            return null;
        }
 
    }
 
    /** Returns the set of String names of the specified variables and names of the axes 
     *      of the the specified variables
     *  @param node the xml node that describes the data
     *  @param varialbes the list of variables that should be in the returned set.
     *     It is a string of comma-separated or space-separated variables. If null,
     *     all variables should be included in the set.
     */

    private static Map getVarAxisSet(Node node, String variables){

        Map returnVal = new HashMap();
        Set touchedVars = new HashSet();
        Set variableSet= new HashSet();
        if(variables != null){
            try{
                StringTokenizer vIt = new StringTokenizer(variables, ",+ ", false);
                while(vIt.hasMoreTokens()){
                   variableSet.add(vIt.nextToken().toUpperCase());
               }
	    }
            catch(NullPointerException npe){}
            if(variableSet.isEmpty()) 
               return returnVal;
        }
  
	Node dataTag = node.getFirstChild();
	for (Node elemTag = dataTag.getFirstChild(); 
	     elemTag != null;
	     elemTag = elemTag.getNextSibling()) {
    
	  if (elemTag.getNodeType()==1) {
              if (elemTag.getNodeName().equals("global")) {
                  if(variables == null) continue;
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null;
                       elemTag1  = elemTag1.getNextSibling()) {
	               if (elemTag1.getNodeType()==1
                            && elemTag1.getNodeName().equals("var")) {
                            String varName = getElemName(elemTag1);
		            if(!touchedVars.contains(varName)
                               && variableSet.contains(varName.toUpperCase())){
                                 touchedVars.add(varName);
                                 returnVal.put(varName, "var");
	                         Node gridNode;
	                         for (gridNode = elemTag1.getFirstChild();
	                              gridNode != null && !gridNode.getNodeName().equals("grid");
	                              gridNode = gridNode.getNextSibling())
	                              {}

	                             if (gridNode == null) continue;

	                         for (int i=0; i<axesNames.length; i+=1) {
	                             Node axis = findAxis (gridNode, axesNames[i]);
	                             if (axis != null) {
	                                 String axisName = encodeName(axis.getFirstChild().getNodeValue());
                                         returnVal.put(axisName, axesNames[i]);
	                             }
	                         }
                            }
                       }
                  }
	      }
              else if (elemTag.getNodeName().equals("datasets")) {
                  for (Node elemTag1 = elemTag.getFirstChild();
                       elemTag1 != null;
                       elemTag1  = elemTag1.getNextSibling()) {
                       if(elemTag1.getNodeType()==1 
                         && elemTag1.getNodeName().equals("dataset")) {
                          for (Node elemTag2 = elemTag1.getFirstChild();
                               elemTag2 != null;
                               elemTag2 = elemTag2.getNextSibling()) {  
	                      if (elemTag2.getNodeType()==1
                                  && elemTag2.getNodeName().equals("var")) {
                                 String varName = getElemName(elemTag2);

		                 if(!touchedVars.contains(varName)
                                    && (variables == null || variableSet.contains(varName.toUpperCase()))){
                                     returnVal.put(varName, "var");
                                     touchedVars.add(varName);
	                             Node gridNode;
	                             for (gridNode = elemTag2.getFirstChild();
	                                  gridNode != null && !gridNode.getNodeName().equals("grid");
	                                  gridNode = gridNode.getNextSibling())
	                                 ;	
	                             if (gridNode == null) continue;

	                             for (int i=0; i<axesNames.length; i+=1) {
	                                 Node axis = findAxis (gridNode, axesNames[i]);
	                                 if (axis != null) {
	                                     String axisName = encodeName(axis.getFirstChild().getNodeValue());
                                             returnVal.put(axisName, axesNames[i]);
	                                 }
	                             }
                                 }
                              }
                          }
                      }
                  }
	      }
	  }
       }
       return returnVal;
    }


    // Finds the axis node if given a grid node and an axis name
    private static Node findAxis (Node gridNode, String axisName) {
  	Node axesNode;
        for(axesNode = gridNode.getFirstChild();
            axesNode !=null;
            axesNode = axesNode.getNextSibling())
        {
            if(axesNode.getNodeType() != 1) continue;
            if(axesNode.getNodeName().equals("axes")) break;
        }

        Node axisNode;
	for (axisNode = axesNode.getFirstChild(); 
	     axisNode != null;
	     axisNode = axisNode.getNextSibling())
	{
	    if (axisNode.getNodeType() != 1) continue;
	    if (axisName.equals(axisNode.getNodeName())) break;
	}
	return axisNode;
    }

    // Gets the name the element
    private static String getElemName(Node node) {
	NamedNodeMap attrMap = node.getAttributes();
	if (attrMap != null) {
	    Node nameNode = attrMap.getNamedItem("name");
	    if (nameNode != null)
		return encodeName(nameNode.getNodeValue());
	}
	return " -=> ERROR!!! unnamed variable <=-";
    }

    //Gets the length of the element if it has a "length" attribute
    private static int getElemLength(Node node) {
	Node child;
	for (child = node.getFirstChild();
	     child != null;
	     child = child.getNextSibling())
	  if (child.getNodeType() == 1 && 
	      child.getNodeName().equals("length"))
	    return Integer.parseInt(child.getFirstChild().getNodeValue());
	return 0; // ERROR!!! no length found
    }

    //Gets the attribute table for an element
    private static void getAttributes(Node node, AttributeTable attrs) {
	Node child;
	for (child = node.getFirstChild(); child != null;
	     child = child.getNextSibling()) 
	{
	    if (child.getNodeType() == 1) {
		try {
		int attrIdx;
		String childName = child.getNodeName();
		for (attrIdx = 0; attrIdx < standardAttrName.length &&
  		     !childName.equals(standardAttrName[attrIdx]);
		     attrIdx += 1);
		switch (attrIdx) {
		case 1: // units &
		case 2: // title : add STRING attribute
		case 6:
		case 7:
		case 8:
		    attrs.appendAttribute(childName,
				 Attribute.STRING,
			     "\"" +child.getFirstChild().getNodeValue()+"\"");
		    break;
		case 3: // FillValue &
		case 4: // missing_value : add FLOAT32 attribute
		    attrs.appendAttribute(childName,
				 Attribute.FLOAT32,
				 child.getFirstChild().getNodeValue());
		    break;
		case 5: break; // grid, so do nothing for DAS
		case 9: // length &
		case 10:// start &
		case 11:// end : these are for DDS, so ignore
		    break;
		default: // treat as STRING, since all values are strings...
		    attrs.appendAttribute(childName,
				 Attribute.STRING,
			     "\""+child.getFirstChild().getNodeValue()+"\"");
		}
		} catch (Exception e) {}
	    }
	}
    }

    /** Appends dimensions to a array <code>arr</code> in grid <code>grid</code> of 
     *  server side DDS <code>dds</code>  according to document node <code>node</code>.
     *  @param node the given document node
     *  @param dds the server side DDS object
     *  @param arr the array that should be added dimension
     *  @param grid the grid to which the array belongs
     */
    private static void appendDims(Node node, ServerDDS dds, 
				  SDArray arr, SDGrid grid) 
    {
	Node gridNode;
	for (gridNode = node.getFirstChild();
	     gridNode != null && !gridNode.getNodeName().equals("grid");
	     gridNode = gridNode.getNextSibling())
	     {}

	if (gridNode == null) return;
	
	for (int i=0; i<axesNames.length; i+=1) {
	    Node axis = findAxis (gridNode, axesNames[i]);
	    if (axis != null) {
	        try {
		    String axisName = encodeName(axis.getFirstChild().getNodeValue());
		    FerretArray map = (FerretArray)dds.getVariable(axisName);
		    int len = map.getFirstDimension().getSize();
		    arr.appendDim(len, axisName);
		    grid.addVariable((FerretArray)map.clone(), DGrid.MAPS);
	        } catch (Exception e) {}
	    }
	}
    }

    public static String encodeName(String name) {
        if(name==null)
            return null;
        if(name.startsWith("(")&&name.endsWith(")")){
            name ="__" + name.substring(1, name.length()-1) +"__";
            
        }
        return name;
    }

    public static String decodeName(String name) {
        if(name==null)
            return null;
        if(name.length()>4
           &&name.startsWith("__")
           &&name.endsWith("__")){
            name ="(" + name.substring(2, name.length()-2) +")";
            
        }
        return name;
    }

    //Prints the xml node. Recursive function.
    private static void printNode(Node node, int tabs) {
	int i;
	for (i=0; i < tabs; i+=1) System.out.print("  ");
	System.out.print (typeName[node.getNodeType()]);

	switch (node.getNodeType()) {
	case 1: 
	    System.out.print (": "+node.getNodeName());
	    NamedNodeMap attrMap = node.getAttributes();
	    break;
	case 3: System.out.print ("= "+node.getNodeValue()); break;
	}
	Node child;
	for (child = node.getFirstChild(); child != null; 
	     child = child.getNextSibling()) 
          printNode (child, tabs+1);
    }

    // An array of names for DOM node-types
    // (Array indexes = nodeType() values.)
    private static final String[] typeName = {
	"none",
	"Element",
	"Attr",
	"Text",
	"CDATA",
	"EntityRef",
	"Entity",
	"ProcInstr",
	"Comment",
	"Document",
	"DocType",
	"DocFragment",
	"Notation",
    };

    // An array of axis names  
    private static final String[] axesNames = {
	"taxis",
	"zaxis",
	"yaxis",
	"xaxis"};

    // An array of standard atrribute names 
    private static final String[] standardAttrName = {
	"none",
	"units",
	"title",
	"_FillValue",
	"missing_value",
	"grid",
	"regular",
	"modulo",
	"positive",

	// filter these out of DAS
	"length",
	"start",
	"end"
    };

    // An array of standard DDS name 
    private static final String[] standardDDSName = {
	"none",
    };


    /** The main function for unit test
     */
    public static void main(String argv[]) {
	if (argv.length != 1) {
	    System.err.println("Usage: java FerretXMLReader filename");
	    System.exit(1);
	}

	DocumentBuilderFactory factory =
	    DocumentBuilderFactory.newInstance();
	Document document;
	try {
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    document = builder.parse( new File(argv[0]) );

            PrintWriter pw = new PrintWriter(System.out);

//	    printNode (document, 0);
/*
	    DAS das = new DAS();
	    DOM2DAS(document, das, "A,SST,TEMP_20");
	    das.print(pw);

            FerretTypeFactory baseTypeFactory = new FerretTypeFactory();
            ServerDDS dds = new ServerDDS("coads",baseTypeFactory);
            //HashSet hs=getVarAxisSet(document,"SST,A");
            //System.out.print(""+hs);
            DOM2DDS(document, dds, null);
            dds.print(pw);
*/
/*
            List varSet = getVariables(document);
            Iterator vIt = varSet.iterator();

            while(vIt.hasNext())
               System.out.println(vIt.next());
*/
            System.out.println(""+whichDim(document,"A","ZAXLEVITR"));

            pw.flush();
            pw.close();

	    // DOM uses the same exceptions as SAX
	} catch (SAXParseException spe) {
	    // Error generated by the parser
	    System.out.println("\n** Parsing error"
			       + ", line " + spe.getLineNumber()
			       + ", uri " + spe.getSystemId());
	    System.out.println("   " + spe.getMessage() );
	    
	    // Use the contained exception, if any
	    Exception  x = spe;
	    if (spe.getException() != null)
		x = spe.getException();
	    x.printStackTrace();
	    
	} catch (SAXException sxe) {
	    // Error generated during parsing
	    Exception  x = sxe;
	    if (sxe.getException() != null)
		x = sxe.getException();
	    x.printStackTrace();
	    
	} catch (ParserConfigurationException pce) {
	    // Parser with specified options can't be built
	    pce.printStackTrace();
	    
	} catch (IOException ioe) {
	    // I/O error
	    ioe.printStackTrace();
	} catch (Exception e){
            e.printStackTrace();
        }
       
    }
    
}
