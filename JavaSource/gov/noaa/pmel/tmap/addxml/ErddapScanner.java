package gov.noaa.pmel.tmap.addxml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.DAP2Exception;
import opendap.dap.DAS;
import opendap.dap.NoSuchAttributeException;
import opendap.dap.parsers.ParseException;

public class ErddapScanner {

    public static DAS das = new DAS();
    public static InputStream input;
    public static List<String> subsetNames = new ArrayList<String>();
    public static Map<String, AttributeTable> trajIdVar = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> timeVar = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> latVar = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> lonVar = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> zVar = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> data = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> subsets = new HashMap<String, AttributeTable>();
    public static Map<String, AttributeTable> monthOfYear = new HashMap<String, AttributeTable>();
    
    protected static ArrayList<VariableBean> variables = new ArrayList<VariableBean>();
    protected static GridBean gb = new GridBean();
    
    protected static String url = "http://osmc.noaa.gov/erddap/tabledap/";
    protected static String id = "OSMCV4_DUO_SURFACE_TRAJECTORY";
    
    protected static ErddapScannerOptions opts = new ErddapScannerOptions();
    protected static CommandLine cl;

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        try {
            CommandLineParser parser = new GnuParser();
            cl = parser.parse(opts, args);
            url = cl.getOptionValue("url");
            id = cl.getOptionValue("id");
            if ( !url.endsWith("/") ) {
                url = url+"/";
            }
            input = new URL(url+id+".das").openStream();
            das.parse(input);
            AttributeTable global = das.getAttributeTable("NC_GLOBAL");
            Attribute cdm_trajectory_variables_attribute = global.getAttribute("cdm_trajectory_variables");
            AttributeTable variableAttributes = das.getAttributeTable("s");
            if ( cdm_trajectory_variables_attribute != null && variableAttributes != null ) {

                Iterator<String> trajectory_variables_attribute_values = cdm_trajectory_variables_attribute.getValuesIterator();
                if ( trajectory_variables_attribute_values.hasNext() ) {
                    // Work with the first value...  Attributes like ranges can have multiple values...
                    String trajectory_variables_value = trajectory_variables_attribute_values.next();
                    String[] trajectory_variables = trajectory_variables_value.split(",");
                    for (int i = 0; i < trajectory_variables.length; i++) {
                        String tv = trajectory_variables[i].trim();
                        if ( !tv.equals("") ) {
                            subsetNames.add(tv);
                        }
                    }
                } else {
                    System.err.println("No CDM trajectory variables found in the cdm_trajectory_variables global attribute.");
                }
                // Classify all of the variables...

                Enumeration names = variableAttributes.getNames();
                if ( !names.hasMoreElements() ) {
                    System.out.println("No variables found in this data collection.");
                }
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    AttributeTable var = variableAttributes.getAttribute(name).getContainer();
                    if ( subsetNames.contains(name) ) {
                        if ( var.hasAttribute("cf_role") && var.getAttribute("cf_role").getValueAt(0).equals("trajectory_id") ) {
                            trajIdVar.put(name, var);
                        } else {
                            subsets.put(name, var);
                        }
                    } else {
                        // Look at the attributes and classify any variable that falls in here as either time, lat, lon, z or a data variable.
                        if ( var.hasAttribute("_CoordinateAxisType") ) {
                            String type = var.getAttribute("_CoordinateAxisType").getValueAt(0);
                            if ( type.toLowerCase().equals("time") ) {
                                timeVar.put(name, var);
                            } else if ( type.toLowerCase().equals("lon") ) {
                                lonVar.put(name, var);
                            } else if ( type.toLowerCase().equals("lat") ) {
                                latVar.put(name, var);
                            } else if ( type.toLowerCase().equals("height") ) {
                                zVar.put(name, var);GridBean gb = new GridBean();
                            }
                        } else {
                            if ( name.toLowerCase().contains("tmonth") ) {
                                monthOfYear.put(name, var);
                            }
                            data.put(name, var);
                        }

                    } 
                }
                // DEBUG what we've got so far:
                if ( !trajIdVar.keySet().isEmpty() ) {
                    String name = trajIdVar.keySet().iterator().next();
                    System.out.println("Trajectory ID variable:");
                    System.out.println("\t "+name);
                }
                System.out.println("Subset variables:");

                for (Iterator subIt = subsets.keySet().iterator(); subIt.hasNext();) {
                    String key = (String) subIt.next();
                    System.out.println("\t "+key);
                }
                if ( !timeVar.keySet().isEmpty() ) {
                    String name = timeVar.keySet().iterator().next();
                    System.out.println("Time variable:");
                    System.out.println("\t "+name);
                }
                if ( !lonVar.keySet().isEmpty() ) {
                    String name = lonVar.keySet().iterator().next();
                    System.out.println("Lon variable:");
                    System.out.println("\t "+name);
                }
                if ( !latVar.keySet().isEmpty() ) {
                    String name = latVar.keySet().iterator().next();
                    System.out.println("Lat variable:");
                    System.out.println("\t "+name);
                }
                if ( !zVar.keySet().isEmpty() ) {
                    String name = zVar.keySet().iterator().next();
                    System.out.println("Z variable:");
                    System.out.println("\t "+name);
                }
                if ( !monthOfYear.keySet().isEmpty() ) {
                    String name = monthOfYear.keySet().iterator().next();
                    System.out.println("Month of year variable:");
                    System.out.println("\t "+name);
                }
                
                System.out.println("Data variables:");

                for (Iterator subIt = data.keySet().iterator(); subIt.hasNext();) {
                    String key = (String) subIt.next();
                    System.out.println("\t "+key);
                }
               
                Element datasetsE = new Element("datasets");
                Element gridsE = new Element("grids");
                Element axesE = new Element("axes");
                // Build the LAS configuration.
                DatasetBean db = new DatasetBean();
                db.setElement(id);
                db.setName(id);            

                db.setUrl(url);
                // Build the grid...
                
                String gridid = "grid-"+id;
                gb.setElement(gridid);
                
                UniqueVector axes = new UniqueVector();
                if ( !timeVar.keySet().isEmpty() ) {
                    String name = timeVar.keySet().iterator().next();
                    AttributeTable var = timeVar.get(name);
                    db.setProperty("tabledap_access", "time", name);
                    AxisBean ab = new AxisBean();
                    ab.setElement(name+"-"+id);
                    ab.setType("t");
                    Attribute ua = var.getAttribute("units");
                    if ( ua != null ) {
                        String units = ua.getValueAt(0);
                        db.setProperty("tabledap_access", "time_units", units);
                    }
                    ArangeBean arb = new ArangeBean();
                    arb.setStart("UNKNOW_START");
                    arb.setStep("UNKNOW_STEP");
                    arb.setSize("UNKNOW_SIZE");
                    ab.setArange(arb);
                    axes.add(ab);
                }
                if ( !lonVar.keySet().isEmpty() ) {
                    String name = lonVar.keySet().iterator().next();
                    db.setProperty("tabledap_access", "longitude", name);
                    AttributeTable var = lonVar.get(name);
                    AxisBean ab = new AxisBean();
                    ab.setElement(name+"-"+id);
                    ab.setType("x");
                    Attribute ua = var.getAttribute("units");
                    if ( ua != null ) {
                        String units = ua.getValueAt(0);
                        ab.setUnits(units);
                    }
                    Attribute ar = var.getAttribute("actual_range");
                    if ( ar != null ) {
                        String start = ar.getValueAt(0);
                        String end = ar.getValueAt(1);
                        double size = Double.valueOf(end) - Double.valueOf(start);
                        ArangeBean arb = new ArangeBean();          

                        arb.setStart(start);
                        arb.setStep("1.0");
                        arb.setSize(String.valueOf(size));
                        ab.setArange(arb);
                        db.setProperty("tabledap_access", "lon_domain", start+":"+end);

                    } else {
                        ArangeBean arb = new ArangeBean();
                        arb.setStart("UNKNOW_START");
                        arb.setStep("UNKNOW_STEP");
                        arb.setSize("UNKNOW_SIZE");
                        ab.setArange(arb);
                        db.setProperty("tabledap_access", "lon_domaine", "UNKNOWN:UNKNOWN");
                    }
                    axes.add(ab);
                }
                if ( !latVar.keySet().isEmpty() ) {           

                    String name = latVar.keySet().iterator().next();  
                    db.setProperty("tabledap_access", "latitude", name);
                    AttributeTable var = latVar.get(name);
                    AxisBean ab = new AxisBean();
                    ab.setElement(name+"-"+id);
                    ab.setType("y");
                    Attribute ua = var.getAttribute("units");
                    if ( ua != null ) {
                        String units = ua.getValueAt(0);
                        ab.setUnits(units);
                    }
                    Attribute ar = var.getAttribute("actual_range");
                    if ( ar != null ) {
                        String start = ar.getValueAt(0);
                        String end = ar.getValueAt(1);
                        double size = Double.valueOf(end) - Double.valueOf(start);
                        ArangeBean arb = new ArangeBean();
                        arb.setStart(start);
                        arb.setStep("1.0");
                        arb.setSize(String.valueOf(size));
                        ab.setArange(arb);
                    } else {
                        ArangeBean arb = new ArangeBean();
                        arb.setStart("UNKNOW_START");
                        arb.setStep("UNKNOW_STEP");
                        arb.setSize("UNKNOW_SIZE");
                        ab.setArange(arb);
                    }
                    axes.add(ab);
                }
                /*
                 * Right now we cannot do anything but surface trajectories so we're going to ignore the depth variable.
                 
                if ( !zVar.keySet().isEmpty() ) {
                    String name = zVar.keySet().iterator().next();
                    AttributeTable var = zVar.get(name);
                    AxisBean ab = new AxisBean();
                    ab.setElement(name+"-"+id);
                    ab.setType("z");
                    Attribute ua = var.getAttribute("units");
                    if ( ua != null ) {
                        String units = ua.getValueAt(0);
                        ab.setUnits(units);
                    }
                    Attribute ar = var.getAttribute("actual_range");
                    if ( ar != null ) {
                        String start = ar.getValueAt(0);
                        String end = ar.getValueAt(1);
                        double size = Double.valueOf(end) - Double.valueOf(end);
                        ArangeBean arb = new ArangeBean();
                        arb.setStart(start);
                        arb.setStep("1.0");
                        arb.setSize(String.valueOf(size));
                    ab.setArange(arb);  
                    } else {
                        ArangeBean arb = new ArangeBean();
                        arb.setStart("UNKNOW_START");
                        arb.setStep("UNKNOW_STEP");
                        arb.setSize("UNKNOW_SIZE");
                        ab.setArange(arb);
                    }
                    axes.add(ab);            System.err.println(e.getMessage());

                }
                */
                gb.setAxes(axes);

                Element cons = new Element("constraints");
                // Set up the variables...
                
                Element idcg = new Element("constraint_group");
                
                String idname = trajIdVar.keySet().iterator().next();
                AttributeTable idvar = trajIdVar.get(idname);
                VariableBean idvb = addSubset(idname, idvar);
                idvb.addAttribute("color_by", "true");
                idvb.addAttribute("trajectory_id", "true");
                idvb.addAttribute("grid_type", "trajectory");
                variables.add(idvb);
                
                idcg.setAttribute("name", "Individual Cruise(s)");
                idcg.setAttribute("type", "selection");
                
                Element idc = new Element("constraint");
                idc.setAttribute("name","Select By");
                Element idv = new Element("variable");
                idv.setAttribute("IDREF", id+"-"+idname);
                Element idkey = new Element("key");
                idkey.setText(idname);
                idc.addContent(idv);
                idc.addContent(idkey);
                idcg.addContent(idc);
                cons.addContent(idcg);
                
                Element subsetcg = new Element("constraint_group");
                subsetcg.setAttribute("type", "subset");
                subsetcg.setAttribute("name", "by Metadata");
                
                for (Iterator subsetIt = subsets.keySet().iterator(); subsetIt.hasNext();) {
                    
                    String name = (String) subsetIt.next();
                    AttributeTable var = subsets.get(name);
                    VariableBean vb = addSubset(name, var);
                    vb.addAttribute("grid_type", "trajectory");
                    variables.add(vb);
                    Element c = new Element("constraint");
                    c.setAttribute("type", "subset");
                    c.setAttribute("widget", "list");
                    Element v = new Element("variable");
                    v.setAttribute("IDREF", id+"-"+name);
                    Element key = new Element("key");
                    key.setText(name);
                    c.addContent(v);
                    c.addContent(key);
                    subsetcg.addContent(c);
                }
                cons.addContent(subsetcg);

                int i = 0;
                for (Iterator dataIt = data.keySet().iterator(); dataIt.hasNext();) {
                    String name = (String) dataIt.next();
                    if ( i == 0 ) db.setProperty("tabledap_access", "dummy", name);
                    i++;
                    AttributeTable var = data.get(name);
                    VariableBean vb = new VariableBean();
                    vb.setElement(id+"-"+name);
                    
                    vb.setUrl("#"+name);
                    Attribute ua = var.getAttribute("units");
                    if ( ua != null ) {
                        String units = ua.getValueAt(0);
                        vb.setUnits(units);
                    } else {
                        vb.setUnits("none");
                    }
                    Attribute ln = var.getAttribute("long_name");
                    if ( ln != null ) {
                        String longname = ln.getValueAt(0);
                        vb.setName(longname);
                    } else {
                        vb.setName(name);
                    }
                    vb.setUrl("#"+name);
                    vb.setGrid(gb);
                    vb.addAttribute("grid_type", "trajectory");
                    variables.add(vb);            

                }
                
                
                db.addAllVariables(variables);
                File outputFile = new File("las_from_scanerddap.xml");
                
              

                // Add all the tabledap_access properties
                
                db.setProperty("tabledap_access", "trajectory_id", trajIdVar.keySet().iterator().next());
                db.setProperty("tabledap_access", "server", "TableDAP Trajectory");
                db.setProperty("tabledap_access", "title", "Need a way to get the title from the server.");
                
                db.setProperty("tabledap_access", "id", id);
                
                db.setProperty("ui", "default", "file:ui.xml#Trajectories");
                
                if ( !monthOfYear.keySet().isEmpty() ) {
                    String name = monthOfYear.keySet().iterator().next();
                    String mid = id+"-"+name;
                    Element season = new Element("constraint_group");
                    season.setAttribute("type", "season");            

                    season.setAttribute("name", "by Season");
                    Element con = new Element("constraint");
                    con.setAttribute("widget", "month");
                    Element variable = new Element("variable");
                    variable.setAttribute("IDREF", mid);
                    Element key = new Element("key");
                    key.setText(name);
                    con.addContent(key);
                    con.addContent(variable);
                    season.addContent(con);
                    cons.addContent(season);
                }


                Element vrcg = new Element("constraint_group");
                vrcg.setAttribute("type", "variable");
                vrcg.setAttribute("name", "by Variable");
                cons.addContent(vrcg);
                
                Element valcg = new Element("constraint_group");
                valcg.setAttribute("type", "valid");
                valcg.setAttribute("name", "by Valid Data");
                cons.addContent(valcg);
                
                
                Element d = db.toXml();
                d.addContent(cons);
                datasetsE.addContent(d);            

                
                
                gridsE.addContent(gb.toXml());
                for (Iterator axesIt = axes.iterator(); axesIt.hasNext();) {
                    AxisBean ab = (AxisBean) axesIt.next();
                    axesE.addContent(ab.toXml());
                }
                outputXML(outputFile, datasetsE, false);
                outputXML(outputFile, gridsE, true);
                outputXML(outputFile, axesE, true);
            }
            if ( cdm_trajectory_variables_attribute == null ) {
                System.err.println("No cdm_trajectory_variables global attribute found in this data set.");
            }
            if ( variableAttributes == null ) {
                System.err.println("No variables found.");
            }
        } catch (NoSuchAttributeException e) {
            System.err.println("Error opening: "+url+id+" "+e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("Error opening: "+url+id+" "+e.getMessage());
        } catch (ParseException e) {
            System.err.println("Error opening: "+url+id+" "+e.getMessage());
        } catch (IOException e) {
            System.err.println("Error opening: "+url+id+" "+e.getMessage());
        } catch (DAP2Exception e) {
            System.err.println("Error opening: "+url+id+" "+e.getMessage());
        } catch (org.apache.commons.cli.ParseException e) {
            System.err.println("Error opening: "+url+id+" "+e.getMessage());
        }
    }
    static public VariableBean addSubset(String name, AttributeTable var) throws NoSuchAttributeException {
        VariableBean vb = new VariableBean();
        vb.setElement(id+"-"+name);
        Attribute ln = var.getAttribute("long_name");
        if ( ln != null ) {
            String longname = ln.getValueAt(0);
            vb.setName(longname);
        } else {
            vb.setName(name);
        }
        vb.setUnits("text");
        vb.setUrl("#"+name);
        vb.addAttribute("subset_variable", "true");
        vb.setGrid(gb);
        return vb;
    }
    static public void outputXML(File outputFile, Element element, boolean append) {
        try {
            FileWriter xmlout = new FileWriter(outputFile, append);
            org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
            format.setLineSeparator(System.getProperty("line.separator"));
            XMLOutputter outputter =
                new XMLOutputter(format);
            outputter.output(element, xmlout);
            xmlout.write("\n");
            // Close the FileWriter
            xmlout.close();
        }
        catch (java.io.IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
