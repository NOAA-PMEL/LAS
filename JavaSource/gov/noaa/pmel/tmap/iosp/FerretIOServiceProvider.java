/**
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development. 
 */
package gov.noaa.pmel.tmap.iosp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

import ucar.nc2.NCdump;
import ucar.nc2.NetcdfFile;
import ucar.nc2.ParsedSectionSpec;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.iosp.IOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/**
 * This is the implementation of the netCDF Java IO Service Provider interface that turns Ferret scripts
 * and the variables it accesses and defines into an OPeNDAP netCDF data source.  The variables that are
 * defined by the script must be associated with an existing open data set.  E.g.
 * 
 * use levitus_climatology
 * let/d=levitus_climatology temp_20 = temp[d=levitus_climatology,z=0:20@sum]
 * set var/title="surface heat content"/units="deg C" temp_20[d=levitus_climatology]
 * use coads_climatology
 * let/d=coads_climatology sst_5 = SST[d=coads_climatology]*5.0
 * @author Roland Schweitzer
 *
 */
public class FerretIOServiceProvider implements IOServiceProvider {
    static private Logger log = Logger.getLogger(FerretIOServiceProvider.class.getName());
    RandomAccessFile raf;
    static private final long maxHeader = 512;
    
    /**
     * The default constructor, warms up the FerretTool that will be used to run Ferret.
     *
     */
    public FerretIOServiceProvider () {
        super();      
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#close()
     */
    public void close() throws IOException {
    	if ( raf != null ) {
            raf.close();
    	}
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#getDetailInfo()
     */
    public String getDetailInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#isValidFile(ucar.unidata.io.RandomAccessFile)
     */
    public boolean isValidFile(RandomAccessFile raf) throws IOException {
        log.debug("Checking valid file for Ferret netCDF file.");

        long pos = 0;
        long size = raf.length();
        
        byte[] b = new byte[10];
        try {
            while ( pos < size && pos < maxHeader ) {
               raf.seek(pos);
               raf.read(b);
               String magic = new String(b);
               if ( FerretCommands.containsCommand(magic)) {
                   log.debug("Yes, is valid");
                   return true;
               }
               pos++;
            }          
        } catch (IOException e) { } // fall through
        log.debug("No, is not valid");
        return false;
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#open(ucar.unidata.io.RandomAccessFile, ucar.nc2.NetcdfFile, ucar.nc2.util.CancelTask)
     */
    public void open(RandomAccessFile raf, NetcdfFile ncfile,
            CancelTask cancelTask) throws IOException {
        this.raf = raf;
    	FerretTool tool;
		try {
			tool = new FerretTool();
		} catch (Exception e) {
			throw new IOException(e.toString());
		}
        log.debug("Opening " + raf.getLocation());
        raf.seek(0);
        StringReader sr;
        try {
            byte[] b = new byte[(int)raf.length()];
            raf.read(b);
            sr = new StringReader(new String(b));
        } catch (IOException e) {
            log.debug("IO Exception reading the random access file.");
            throw e;
        }


        // Use this script to run Ferret and produce the equivalent of
        // the netCDF header by reading the XML output from Ferret and building all
        // the dimensions, variables and attributes.

        // Run the FerretTool to make the XML.

        String jnl = null;

        StringBuffer inJnl = new StringBuffer();

        BufferedReader jnlBuffReader = new BufferedReader(sr);
        try {
            String line = jnlBuffReader.readLine();

            while (line != null) {
                inJnl.append(line+"\n");
                line = jnlBuffReader.readLine();
            }
        } catch (IOException e) {
        }
        jnl = inJnl.toString();

        String cacheKey = JDOMUtils.MD5Encode(jnl);           
        String xmlHeader = null;
        try {
            xmlHeader = tool.run_header("header.jnl", jnl, cacheKey);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        log.debug("process the XML header in "+xmlHeader);
        // Process the XML.
        Document header = new Document();
        File headerFile = new File(xmlHeader);
        try {
            JDOMUtils.XML2JDOM(headerFile, header);
        } catch (Exception e) {
        	headerFile.renameTo(new File(headerFile.getAbsoluteFile()+".bad"));
            log.error("Error processing header file XML "+e.getLocalizedMessage());
        }
        log.debug("document built "+header.toString());



        
        // First build a hash that identifies the data set to which an axis belongs.
        HashMap<String, String> axis_datasets = new HashMap<String, String>();
        List data = header.getRootElement().getChild("datasets").getChildren("dataset");
        for (Iterator dataIt = data.iterator(); dataIt.hasNext();) {
            Element dataset = (Element) dataIt.next();
            String dataset_name = dataset.getAttributeValue("name");
            List vars = dataset.getChildren("var");
            for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
                Element var = (Element) varIt.next();
                List var_axes = var.getChild("grid").getChild("axes").getChildren();             
                for (Iterator axisIt = var_axes.iterator(); axisIt.hasNext();) {
                    Element axis = (Element) axisIt.next();
                    String axisName = axis.getTextNormalize();
                    axis_datasets.put(axisName, dataset_name);
                }
            }
        }
        log.debug("Finished parsing the XML file.");
        // This creates the dimensions and the coordinate variables from the Ferret XML description.
        List axes = header.getRootElement().getChild("axes").getChildren("axis");
        
        
        
        

        log.debug("Found "+axes.size()+" axis elements.");


        // Keep a list of all the Dimensions created.
        ArrayList<Dimension> allDims = new ArrayList<Dimension>();
       
        for (Iterator axisIt = axes.iterator(); axisIt.hasNext();) {
            Element axisE = (Element) axisIt.next();
            // Collect all the attributes in a HashMap
            List attributeElements = axisE.getChildren("attribute");
            HashMap<String, String> attribute_hash = new HashMap<String, String>();
            if ( attributeElements != null && attributeElements.size() > 1 ) {
            	for (Iterator attIt = attributeElements.iterator(); attIt
						.hasNext();) {
					Element attribute = (Element) attIt.next();
					String value = attribute.getAttributeValue("value");
					String aname = attribute.getAttributeValue("name");
					attribute_hash.put(aname, value);
				}

            } else {
            	List attributes = axisE.getChildren();
            	for (Iterator attIt = attributes.iterator(); attIt.hasNext();) {
            		Element attribute = (Element) attIt.next();
            		log.debug("adding attribute: "+attribute.getName()+" "+attribute.getTextNormalize());
            		String value = attribute.getTextNormalize();
            		String aname = attribute.getName();
            		attribute_hash.put(aname, value);
            	}
            }
            String length = attribute_hash.get("length");
            String name = axisE.getAttributeValue("name");
            log.debug("Working on axis: "+name);

            if ( length != null ) {
                String dimS = length.trim();
                Dimension dim = new Dimension(name, Integer.valueOf(dimS).intValue(), true);
                log.debug("New dim with size: "+dim.getLength());
                List<Dimension> dims = new ArrayList<Dimension>();
                dims.add(dim);
                allDims.add(dim);
                ncfile.addDimension(null, dim);
                Variable coord = new Variable(ncfile, null, null, name);
                coord.setDimensions(dims);
                coord.setDataType(DataType.DOUBLE);
                // See comment about attributes below...
                String datasetName = axis_datasets.get(name);
                if  (datasetName != null ) {
                    coord.addAttribute(new Attribute("dataset", datasetName));
                }
                
                if ( attribute_hash != null && attribute_hash.size() > 1 ) {
                	for (Iterator attIt = attribute_hash.keySet().iterator(); attIt
							.hasNext();) {
						String aname = (String) attIt.next();
						String value = attribute_hash.get(aname).trim();
								
            			try {
            				double dvalue = Double.valueOf(value).doubleValue();
            				coord.addAttribute(new Attribute(aname, new Double(dvalue)));
            			} catch (NumberFormatException nfe) {
            				coord.addAttribute(new Attribute(aname, value));
            			}
					}

                }
                ncfile.addVariable(null, coord);
            }
        }

        log.debug("Finished with coordinate axes variables.");
        /* This creates the data variables form the Ferret XML description (global section).
         * Only grab the one's whose grids are != 'ABSTRACT'.
         * */
        ArrayList<String> globalNames = new ArrayList<String>();
        Element globalE = header.getRootElement().getChild("global");

        if ( globalE != null ) {
            List globalVars = globalE.getChildren("var");

            log.debug("Found "+globalVars.size()+" 'global' variables");

            for (Iterator varIt = globalVars.iterator(); varIt.hasNext();) {
                Element var = (Element) varIt.next();
                String name = var.getAttributeValue("name");                
                // Find the name of the data set to which this variable is being added.
                String dsname = name.replaceAll(" ","");
                int end = dsname.indexOf("]");
                if ( dsname.indexOf(",") > 0 ) {
                    end = Math.min(end, dsname.indexOf(","));
                }
                if ( dsname.contains("D=") ) {
                    dsname = dsname.substring(dsname.indexOf("[D=")+3, end);
                } else if ( dsname.contains("d=")){
                    dsname = dsname.substring(dsname.indexOf("[d=")+3, end);
                } else {
                    dsname="1";
                }
                // Get rid of the [d="dataset"] in the netCDF variable name.
                if ( name.contains("D=") ) {
                    name = name.substring(0, name.indexOf("[D="));
                } else if ( name.contains("d=")){
                    name = name.substring(0, name.indexOf("[d="));
                }
                globalNames.add(name);
                Variable dataVar = new Variable(ncfile, null, null, name);
                List var_axes = var.getChild("grid").getChild("axes").getChildren();
                ArrayList<Dimension> varDims = new ArrayList<Dimension>();
                String direction = "";
                for (Iterator axisIt = var_axes.iterator(); axisIt.hasNext();) {
                    Element axis = (Element) axisIt.next();
                    String axisName = axis.getTextNormalize();
                    String axisType = axis.getName();
                    for (Iterator dimIt = allDims.iterator(); dimIt.hasNext();) {
                        Dimension dim = (Dimension) dimIt.next();
                        if ( dim.getName().equals(axisName) ) {
                            varDims.add(dim);
                            String direc = "";
                            if (axisType.equals("xaxis") ) {
                            	direc = "I";
                            } else if ( axisType.equals("yaxis") ) {
                            	direc = "J";
                            } else if ( axisType.equals("zaxis") ) {
                            	direc = "K";
                            } else if ( axisType.equals("taxis") ) {
                            	direc = "L";
                            }
                            direction = direction + direc;
                        }                     
                    }
                }
                Collections.reverse(varDims);
                dataVar.setDimensions(varDims);
                dataVar.setDataType(DataType.FLOAT);
                dataVar.addAttribute(new Attribute("dataset", dsname));
                /* 
                 * Fixed at of Ferret V6.1.1.  Code deals with either...  See below...
                 *          The netCDF attributes are listed as XML elements.
                 *          Confusing...
                 */
                List attributeElements = var.getChildren("attribute");
                dataVar.addAttribute(new Attribute("direction", direction));
            	boolean abstract_grid = false;
                if ( attributeElements != null && attributeElements.size() > 1 ) {
                	for (Iterator attIt = attributeElements.iterator(); attIt.hasNext();) {
						Element attribute = (Element) attIt.next();
						String aname = attribute.getAttributeValue("name");
						String value = attribute.getAttributeValue("value");
						try {
            				float fvalue = Float.valueOf(value).floatValue();
            				dataVar.addAttribute(new Attribute(aname, new Float(fvalue)));
            			} catch (NumberFormatException nfe) {
            				dataVar.addAttribute(new Attribute(aname, value));
            			}
					}
                	Element grid = var.getChild("grid");
                	if ( grid.getAttributeValue("name").equals("ABSTRACT") ) {
                		abstract_grid = true;
                	}
                } else {
                	List attributes = var.getChildren();
                	for (Iterator attIt = attributes.iterator(); attIt.hasNext();) {
                		Element attribute = (Element) attIt.next();
                		if ( !attribute.getName().equals("grid") ) {
                			String value = attribute.getTextNormalize();
                			String aname = attribute.getName();
                			try {
                				float fvalue = Float.valueOf(value).floatValue();
                				dataVar.addAttribute(new Attribute(aname, new Float(fvalue)));
                			} catch (NumberFormatException nfe) {
                				dataVar.addAttribute(new Attribute(aname, value));
                			}
                		} else {
                			if ( attribute.getAttributeValue("name").equals("ABSTRACT") ) {
                				abstract_grid = true;
                			}
                		}
                	}
                }
                dataVar.addAttribute(new Attribute("virtual", "true"));
                String def = var.getAttributeValue("def");
                if ( def != null ) {
                    dataVar.addAttribute(new Attribute("ferret_definition", def));
                }
                if ( !abstract_grid ) {
                   ncfile.addVariable(null, dataVar);
                }
            }
        }
        // This creates the data variables form the Ferret XML description.
        	log.debug("Found "+data.size()+" 'datasets'");
        	ArrayList<String> varNAMES = new ArrayList<String>();
        	for (Iterator dataIt = data.iterator(); dataIt.hasNext();) {
        		Element dataset = (Element) dataIt.next();
        		String dataset_name = dataset.getAttributeValue("name");
        		List vars = dataset.getChildren("var");
        		for (Iterator varIt = vars.iterator(); varIt.hasNext();) {
        			Element var = (Element) varIt.next();
        			String name = var.getAttributeValue("name");
        			// Maybe I should be ignoring the global names and requiring a data set for each transformed variable.
        			if ( !globalNames.contains(name)) {
        				// Name conflicts should handled in the Ferret script that defines the dataset.
        				// This is not working...  This hack will keep it from crashing -- I think.
        				if ( !varNAMES.contains(name) ) {
        					varNAMES.add(name);

        					Variable dataVar = new Variable(ncfile, null, null, name);
        					List var_axes = var.getChild("grid").getChild("axes").getChildren();
        					ArrayList<Dimension> varDims = new ArrayList<Dimension>();
        					String direction = "";
        					for (Iterator axisIt = var_axes.iterator(); axisIt.hasNext();) {
        						Element axis = (Element) axisIt.next();
        						String axisType = axis.getName();
        						String axisName = axis.getTextNormalize();
        						for (Iterator dimIt = allDims.iterator(); dimIt.hasNext();) {
        							Dimension dim = (Dimension) dimIt.next();
        							if ( dim.getName().equals(axisName) ) { 
        								varDims.add(dim);
        								String direc = "";
        	                            if (axisType.equals("xaxis") ) {
        	                            	direc = "I";
        	                            } else if ( axisType.equals("yaxis") ) {
        	                            	direc = "J";
        	                            } else if ( axisType.equals("zaxis") ) {
        	                            	direc = "K";
        	                            } else if ( axisType.equals("taxis") ) {
        	                            	direc = "L";
        	                            }
        	                            direction = direction + direc;
        							}
        						}
        					}
        					Collections.reverse(varDims);
        					dataVar.setDimensions(varDims);
        					dataVar.addAttribute(new Attribute("direction", direction));
        					dataVar.setDataType(DataType.FLOAT);
        					/*
        					 * Old Ferrets write attributes like this:
        					 * <var name="SITE_NETCODE">
        					 *     <long_name>Contributing radar site network affiliation code</long_name>
        					 *     <_FillValue>-1.000000E+34</_FillValue>
        					 *     <missing_value>-1.000000E+34</missing_value>
        					 *     <ferret_datatype>STRING</ferret_datatype>
        					 *     <infile_datatype>CHAR</infile_datatype>
        					 *     
        					 *     ....
        					 *     
        					 * New Ferrets (v6.1.1+) write them like this:
        					 * 
        					 * <var name="SITE_NETCODE">
        					 *    <attribute name="long_name" value="Contributing radar site network affiliation code" />
        					 *    <attribute name="_FillValue" value="-1.000000E+34" />
        					 *    <attribute name="missing_value" value="-1.000000E+34" />
        					 *    <attribute name="ferret_datatype" value="STRING" />
        					 *    <attribute name="infile_datatype" value="CHAR />
        					 * 
        					 * Try the new way first...
        					 */
        					List attributeElements = var.getChildren("attribute");
        					if ( attributeElements != null && attributeElements.size() > 0 ) {
        						for (Iterator attIt = attributeElements.iterator(); attIt.hasNext();) {
        							Element attribute = (Element) attIt.next();
        							String value = attribute.getAttributeValue("value");
        							String aname = attribute.getAttributeValue("name");
        							try {
        								float fvalue = Float.valueOf(value).floatValue();
                                                                        // TODO A Hack to skip bad scaling information
                                                                        if ( !aname.equals("add_offset") && !aname.equals("scale_factor") ) {
        								    dataVar.addAttribute(new Attribute(aname, new Float(fvalue)));
                                                                        }
        							} catch (NumberFormatException nfe) {
        							        dataVar.addAttribute(new Attribute(aname, value));
        							}
        						}
        					} else {
        						List attributes = var.getChildren();
        						for (Iterator attIt = attributes.iterator(); attIt.hasNext();) {
        							Element attribute = (Element) attIt.next();
        							if ( !attribute.getName().equals("grid") ) {
        								String value = attribute.getTextNormalize();
        								String aname = attribute.getName();
        								try {
        									float fvalue = Float.valueOf(value).floatValue();
                                                                                if ( !aname.equals("add_offset") && !aname.equals("scale_factor") ) {
        								    	    dataVar.addAttribute(new Attribute(aname, new Float(fvalue)));
                                                                                }
        								} catch (NumberFormatException nfe) {
        								    dataVar.addAttribute(new Attribute(aname, value));
        								}
        							}
        						}
        					}
        					dataVar.addAttribute(new Attribute("dataset", dataset_name));
        					ncfile.addVariable(null, dataVar);
        				}
        			}
        		}
        	}
        
        ncfile.addAttribute(null, new Attribute("Conventions", "COARDS"));
        
        log.debug("parsing complete.");

        int nothing = 0;
        nothing++;
    }

    public Array readData(Variable v2, Section section) throws IOException,
	InvalidRangeException {
    	FerretTool tool;
		try {
			tool = new FerretTool();
		} catch (Exception e) {
			throw new IOException(e.toString());
		}
        Array a=null;
        log.debug("Entering read for "+v2.getName()+" and "+section.getRank()+" ranges.");

        String varname = v2.getName();
        String readname = v2.getName();
        boolean isCoordinateVariable = false;
        if( v2.isCoordinateVariable() ) {
        	readname = "COORDS";
        	isCoordinateVariable = true;
        }
        Attribute dataset_attr = v2.findAttribute("dataset");
        String dataset="";
        if ( dataset_attr != null ) {
            dataset = dataset_attr.getStringValue();
            
        }
        
        String direction = "";
        Attribute direction_attr = v2.findAttribute("direction");
        if ( direction_attr != null ) {
            direction = direction_attr.getStringValue();
        }

        String jnl = null;

        StringBuffer inJnl = new StringBuffer();
        raf.seek(0);
        StringReader sr;
        try {
            byte[] b = new byte[(int)raf.length()];
            raf.read(b);
            sr = new StringReader(new String(b));
        } catch (IOException e) {
            log.debug("IO Exception reading the random access file.");
            throw e;
        }
        BufferedReader jnlBuffReader = new BufferedReader(sr);
        try {
            String line = jnlBuffReader.readLine();

            while (line != null) {
                inJnl.append(line+"\n");
                line = jnlBuffReader.readLine();
            }
        } catch (IOException e) {
        	log.debug("IO Exception reading the random access file.");
            throw e;
        }
        jnl = inJnl.toString();

        String cacheKey = JDOMUtils.MD5Encode(jnl);   
        String filename = tool.getTempDir()+cacheKey+File.separator+"data_"+varname+"_"+section.toString()+".nc";
        String temp_filename = filename+".tmp";
        // Simplest form of caching is that the exact file we need already exists.

        File datatemp = new File(filename);
        File temp_file = new File(temp_filename);
        // Create a range section that pulls out the whole block from the temporary data file.
        ArrayList<Range> newsection = new ArrayList<Range>(); 

        if ( !datatemp.exists() ) {
        	if ( temp_file.exists() ) {
        		// Somebody else is making this file.  Wait for it.
        		int trys = 0;
        		long interval = 2000; // Wait two seconds each time.
        		long timeout = tool.ferretConfig.getTimeLimit()*1000;  // Time out in seconds, convert to millis
        		int limit = (int) (timeout/interval);
        		while ( !datatemp.exists() && trys < limit) {
        			try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						throw new IOException("Interrupted waiting for temporary file "+e.toString());
					}
					trys++;
        		}
        		if ( !datatemp.exists() ) {
        			throw new IOException("Temporary data file unavailable after wait");
        		}
        	} else {
        		String slice;
        		StringBuffer indx;
        		if (isCoordinateVariable) {
        			indx = new StringBuffer("go get_coord \""+temp_file+"\" "+"\""+dataset+"\" "+varname+" "+direction+" ");
        		} else {
        			indx = new StringBuffer("go get_datavar \""+temp_file+"\" "+"\""+dataset+"\" "+varname+" "+direction+" ");
        		}
        		// This is the get_data order XYZT
        		Section ferret_section = new Section(section);


        		for (int s = ferret_section.getRank() - 1; s >= 0 ; s--) {
        			Range range = (Range) ferret_section.getRange(s);
        			int start = range.first()+1;
        			int end = range.last()+1;
        			indx.append(start+" "+end+" "+range.stride()+" ");
        		}

        		// Before we go off and run Ferret see if we can get the data from
        		// an existing file.

        		slice = jnl+"\n"+indx.toString();
        		log.debug("Using jnl: "+slice);
        		try {
        			
        			tool.run("data.jnl", slice, cacheKey, temp_filename, filename);
        			
        		} catch (Exception e) {
        			throw new IOException("Unable run data extract tool "+e.toString());
        		}       		
        	}
        } else {
        	log.debug("Cache hit on: "+filename);
        }
        // One way or another we think we have a file.  Try to use it.
        for (Iterator rangeIt = section.getRanges().iterator(); rangeIt.hasNext();) {
        	Range range = (Range) rangeIt.next();
        	Range newrange = new Range(0, range.length()-1, 1);
        	newsection.add(newrange);
        }


        log.debug("Attempting to open data file: "+filename);
        NetcdfFile nds = null;
        try {
        	nds = NetcdfFile.open(filename, null);
        	log.debug("Attempting to find variable : "+readname);
        	Variable v = nds.findVariable(readname);
        	if ( readname.equals("COORDS")) {
        		List dims = v.getDimensions();
        		// Should be only 1.
        		if ( dims.size() < 0 || dims.size() > 1 ) {
        			log.error("A coordinate variable has more than one dimension.");
        		}
        		Dimension cdim = (Dimension) dims.get(0);
        		String name = cdim.getName();
        		v = nds.findVariable(name);
        	}
        	log.debug("Attempting to read var: "+readname+" with ranges "+Range.makeSectionSpec(newsection));
        	a = v.read(newsection);
        	log.debug("Finished reading variable data.");
        } catch (IOException e ) {
        	log.error("Exception opening netCDF data file. "+e.getLocalizedMessage());
        	throw e;
        } finally {
        	if ( nds != null ) {
        		try {
        			nds.close();
        		} catch (IOException e){
        			log.error("Exception closing the netCDF data file."+e.getMessage());
        		}
        	}
        }
        return a;
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#readNestedData(ucar.nc2.Variable, java.util.List)
     */
    public Array readNestedData(Variable v2, List section) throws IOException,
            InvalidRangeException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#setSpecial(java.lang.Object)
     */
    public void setSpecial(Object special) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#sync()
     */
    public boolean sync() throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#syncExtend()
     */
    public boolean syncExtend() throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ucar.nc2.IOServiceProvider#toStringDebug(java.lang.Object)
     */
    public String toStringDebug(Object o) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static void main(String[] args) {
    	/*
        try {
            log.debug("Registering gov.noaa.pmel.tmap.iosp.FerretIOServiceProvider");
            NetcdfFile.registerIOProvider("gov.noaa.pmel.tmap.iosp.FerretIOServiceProvider");
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	 */
    	//String filename = "http://porter.pmel.noaa.gov:8920/thredds/dodsC/las/coads_climatology_cdf/data_coads_climatology.jnl";
    	//String filename = "/home/porter/rhs/data/coads_climatology.cdf";

    	String filename = "http://porter.pmel.noaa.gov:8920/thredds/dodsC/las/NOAA_NCEP_EMC_CMB_Ocean_Analysis_ml/data__iridl.ldeo.columbia.edu_SOURCES_.NOAA_.NCEP_.EMC_.CMB_.Pacific_.monthly_dods.jnl";


    	//String filename = "http://strider.weathertopconsulting.com:8880/thredds/dodsC/las/coads_climatology_cdf/data_coads_climatology.jnl";
    	//String expr = 		"_expr_{levitus_climatology}{let airt_regrid=airt[d=1,t=\"15-Jan\":\"15-Dec\"@ave];let temp_regrid=temp[d=2,gxy=airt_regrid[d=1]]}";
    	String inner = "http://porter.pmel.noaa.gov:8920/thredds/dodsC/las/levitus_climatology_cdf/coads_climatology_cdf.jnl";
    	String ninner = "";
    	log.setLevel(Level.DEBUG);
    	try {
    		ninner = URLEncoder.encode(inner, "UTF-8");
    	} catch (UnsupportedEncodingException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    	}
    	String expr = 		"_expr_{"+ninner+"}{let temp_1_regrid=temp[d=1,z=5.00:75.00@ave];let sst_2_regrid=sst[d=2,t=\"15-Jan\":\"15-Mar\"@ave];let sst_2_regrid_2_regrid=sst_2_regrid[d=2,gxy=temp_1_regrid[d=1]]}";

    	try {
    		filename = filename + URLEncoder.encode(expr, "UTF-8");
    	} catch (UnsupportedEncodingException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	filename = "http://strider.weathertopconsulting.com:8880/thredds/dodsC/las/id-2c2b44e493/data_ocean_atlas_subset.jnl";
    	NetcdfDataset ncd = null;
    	if ( filename.startsWith("http")) {
    		System.out.println(filename);
    		//System.exit(0);
    	}
    	try {
    		log.debug("Calling openDataset");
    		ncd = NetcdfDataset.openDataset(filename);
    		log.debug("Finished openDataset");
    	} catch (IOException ioe) {
    		log.error("trying to open " + filename + "  " + ioe);
    	} finally { 
    		if (null != ncd) {
    			try {
    				List variables = ncd.getVariables();
    				StringBuffer vars = new StringBuffer();
    				for (Iterator vIt = variables.iterator(); vIt.hasNext();) {
    					Variable v = (Variable) vIt.next();
    					if ( v.isCoordinateVariable() ) {
    						if ( vars.length() > 0 ) vars.append(";");
    						vars.append(v.getName());
    					} 
    				}
    				//NCdump.print(ncd, System.out, true, true, false, true, vars.toString(), null);
    				for (Iterator vIt = variables.iterator(); vIt.hasNext();) {
    					Variable v = (Variable) vIt.next();
    					if ( v.isCoordinateVariable() ) {
    						log.debug("reading "+v.getName());
    						int[] shape = v.getShape();
    						int[] origin = new int[shape.length];
    						for ( int s = 0; s < shape.length; s++) {
    							if ( shape[s] > 7 ) {
    								origin[s] = shape[s]/2;
    							} else {
    								origin[s] = 0;
    							}
    							shape[s] = Math.min(3,shape[s]);
    							log.debug("for dimension s="+s+" setting origin="+origin[s]+" shape="+shape[s]);
    						}
    						Array a = v.read(origin, shape);
    						IndexIterator it = a.getIndexIterator();
    						log.debug("Iterate on the array.");
    						while ( it.hasNext() ) {
    							float val = it.getFloatNext();
    							log.debug("Value for "+it.toString()+ "=" +val);
    						}
    					}
    				}
    				ncd.close();
    			} catch (IOException ioe) {
    				log.error("trying to close " + filename + "  " + ioe.toString());
    			} catch (InvalidRangeException ire) {
    				log.error("bad range: "+ ire.toString());
    			}
    		}
    	}
    }

    public String getDataDir() {
        try {
			return new FerretTool().getDataDir();
		} catch (Exception e) {
			return "";
		}
    }

	public Array readSection(ParsedSectionSpec arg0) throws IOException,
			InvalidRangeException {
		// TODO Auto-generated method stub
		return null;
	}

	public long readToByteChannel(Variable arg0, Section arg1,
			WritableByteChannel arg2) throws IOException, InvalidRangeException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object sendIospMessage(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFileTypeDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFileTypeId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFileTypeVersion() {
		// TODO Auto-generated method stub
		return null;
	}

}
