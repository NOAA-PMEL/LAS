// $Id: Constrain_compareBean.java,v 1.10.4.1 2005/05/19 21:25:05 rhs Exp $
package gov.noaa.pmel.tmap.las.luis;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.*;

import org.apache.velocity.*;
import javax.servlet.http.HttpServletResponse;
import gov.noaa.pmel.tmap.las.luis.db.*;
import gov.noaa.pmel.tmap.las.luis.map.*;
import java.sql.SQLException;
import javax.servlet.ServletException;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import java.text.DecimalFormat;

public class Constrain_compareBean extends ConstrainBean {
   protected void setHandler(TemplateSession session){
      mHandler = new FormParamHandler.CompareVariable(0, session);
   }

   // Keep some state information here.

   Vector mArgs = new Vector();
   String mArg;

   public void init(TemplateContext tc) throws SQLException, ServletException{
      super.init(tc);

      DecimalFormat format = new DecimalFormat("###.####");
      HttpServletRequest req = tc.getServletRequest();
      HttpServletResponse resp = tc.getServletResponse();
      TemplateSession session = Utils.getSession(req);
      SessionTemplateContext sessionContext = mSession.getSessionContext();

      String[] variables = 
         mSession.getSessionObject().getCompareVariables(0).get("variables");
      if (variables == null || variables.length == 0){
         throw new IdNotFoundException();
      }
      if ( !isComparable(variables[0]) ) {
         try {
            Log.debug(this,"First variable is not comparable.");
            resp.sendRedirect("dataset_compare1");
            return;
         }
         catch (java.io.IOException e) {
            throw new 
               ServletException("Could not redirect to dataset_compare1 "+e.getMessage());
         }
      }
      Log.debug(this, "Input to rc.");
      Log.debug(this, "mView="+mView);
      for ( int i =0; i < variables.length; i++ ) {
         Log.debug(this, "variables["+i+"]="+variables[i]);
      }
      RegionConstraint rc = RegionConstraint.getInstance(variables, mView);

      variables = mSession.getSessionObject().getCompareVariables(1).get("variables");
      if (variables == null || variables.length == 0){
         throw new IdNotFoundException();
      }
      if ( !isComparable(variables[0]) ) {
         try {
            Log.debug(this,"Second variable is not comparable.");
            resp.sendRedirect("dataset_compare2");
            return;
         }
         catch (java.io.IOException e) {
            throw new 
               ServletException("Could not redirect to dataset_compare2 "+e.getMessage());
         }
      }

      Log.debug(this, "Input to crc.");
      Log.debug(this, "mView="+mView);
      for ( int i =0; i < variables.length; i++ ) {
         Log.debug(this, "variables["+i+"]="+variables[i]);
      }
      RegionConstraint crc = RegionConstraint.getInstance(variables, mView);
      RegionConstraint rcarray[] = new RegionConstraint[2];
      rcarray[0] = rc;
      rcarray[1] = crc;
      sessionContext.setCompareRegion(rcarray);
      ICategory c[] = new ICategory[2];
      c[0] = rc.getCategory();
      c[1] = crc.getCategory();
      sessionContext.setCompareCategory(c);

      Vector[] dsetArray = new Vector[] {DatasetItem.getItems(c[0].getParentid()),
      DatasetItem.getItems(c[1].getParentid())};
      sessionContext.setCompareDatasets(dsetArray);

      // Manage the non-java map interface.
      if ( !sessionContext.getUseJava().booleanValue() ) {

         MapgenBean.MapgenBeanStuff stuff =
              (MapgenBean.MapgenBeanStuff)sessionContext.get("mapgen");
         MapGenerator gen = null;

         MapStateBean mapState = (MapStateBean) sessionContext.getMapState();

         Log.debug(this,"Redirect is "+mapState.getRedirect());

         if ( mapState.getRedirect() ) {

            if ( mapState.getXlo_compare() == null ) {
               mapState.setXlo_compare(crc.getAxisLo("x"));
            }

            if ( mapState.getXhi_compare() == null ) {
               mapState.setXhi_compare(crc.getAxisHi("x"));
            }

            if ( mapState.getYlo_compare() == null ) {
               mapState.setYlo_compare(crc.getAxisLo("y"));
            }

            if ( mapState.getYhi_compare() == null ) {
               mapState.setYhi_compare(crc.getAxisHi("y"));
            }

            if ( stuff != null ) {
               gen = stuff.gen;
            }
            else {
               // Fire up the map generator.  Should only happen in one place
               // between this an super class, but here it is a second time.
               // Bad dog. 
               LiveMap livemap = sessionContext.getLivemap();
               Image image = livemap.getCurrentImage();
               byte[] image_data = image.getImage();
               try {
                  stuff = new MapgenBean.MapgenBeanStuff(
                        new MapGenerator(image_data,320,160));
                  String sxlo = livemap.getXlo();
                  String sxhi = livemap.getXhi();
                  String sylo = livemap.getYlo();
                  String syhi = livemap.getYhi();
                  double xlo = Double.parseDouble(sxlo);
                  double xhi = Double.parseDouble(sxhi);
                  double ylo = Double.parseDouble(sylo);
                  double yhi = Double.parseDouble(syhi);
                  Log.debug(this, "image coords: "+xlo+", "+xhi+", "+ylo+", "+yhi);
                  stuff.gen.setImageCoordinates(xlo,xhi,ylo,yhi);
               } catch (Exception e){
                  throw new ServletException(e.getMessage());
               }
               sessionContext.put("mapgen",stuff);
               gen = stuff.gen;
               mapState.setValidXlo(rc.getAxisLo("x"));
               mapState.setValidXhi(rc.getAxisHi("x"));
               mapState.setValidYlo(rc.getAxisLo("y"));
               mapState.setValidYhi(rc.getAxisHi("y"));
            }
            FormParameters state = session.getSessionObject().getConstrainState();
            if ( state != null ) {
               mapState.setCurrentTool(getToolType(new String[] {rc.getViewRegion()}));
            }
            if ( gen != null) {
               setMarkerToolType(mapState, stuff);
            }
            Rectangle2D toolRect = null;
            mapState.setNeedTwoVars(false);
            // While in comparison mode any of these map tool modes
            // allow selection of a range for each variable thus
            // the Var 1 and Var 2 radio buttons must be displayed.
            String tool;
            String[] whvar;
            if ( state != null ) {
               tool = getToolType(new String[] {rc.getViewRegion()});
               if ( tool.equals("PT") || tool.equals("X") || tool.equals("Y") ) {  
                  mapState.setNeedTwoVars(true);
               }
               // Check to see which variable is selected in the interface
               // If there is none selected set it to "1".
               whvar = state.get("whvar");
               if ( whvar == null ) {
                  whvar = new String[1];
                  whvar[0] = "1";
                  state.add("whvar", whvar);
                  Log.debug(this, "Forcing whvar to 1");
               }
               else if ( whvar != null ) {
                  if (whvar[0].equals("1")) {
                     mapState.setCurrentVariable(1);
                  }
                  else if (whvar[0].equals("2")) {
                     mapState.setCurrentVariable(2);
                  }
               }
            }
            else {
               tool = "XY";
            }

            int myIndex = mapState.getCurrentVariable() - 1;
            if ( gen != null ) {
               gen.setMarkerIndex(myIndex);
               toolRect = gen.getMarkerCoordinatesByIndex(myIndex);
            }
            /* This Bean is a strange amalgamation of many different techniques for
             * passing and maintaining the state of the user interface.
             *
             * The first difficulty is that there is no convenient way to get the
             * mouse click coordinates in an image from an old browser.  In order for
             * the interface to work with old browser, the ismap construct must be used
             * on the client.  The ismap interface passes the click coordinates on the
             * URL query (the text after the "?") so this Bean must parse and crack that
             * information rather than use all of the tidy mechanisms 
             * designed and implemented by Joe S.
             *
             * However, in order to maintain the state of the UI constrains form, the
             * tidy mechanisms must be used to handle changes in the View and Region
             * menu.
             *
             * Changes in the view menu get handled "automatically" becuase this Bean
             * forces the tool type to be set according to the current view everytime
             * it gets invoked.
             *
             * Changes to the Region menu previously were handled by the interaction 
             * of client-side code (JavaScript and the Map applet).  Since there is
             * no Map applet in this case, we must maintain that state using the
             * here and we must perform the requested region change
             * operation in this Bean.  Therefore the first thing we have to check
             * is to see if this Bean was invoked as a result of a change in the Region
             * menu.  If it was, we must deal with that first.
             *
             * Also consider that when the non-java constrain page loads the map image
             * is retrived from the servlet engine everytime.  Therefore it is possible
             * for the servlet to perform a transformation on the map before sending the
             * image bytes.  This code takes advantage of that fact and makes some
             * map transformations happen right before the map is sent to the browser
             * by interpreting a command in the query string of the request for the image.
             */

             String action="none";
             String[] actions = {"0"};
             if ( state != null ) {
                actions = state.get("action");
             }
             if (actions != null) {
                action=actions[0];
             }

            // True if the user pulled down the region menu.
            if ( action.equals("changeRegion") ) {
               // Get the values for the selected region.
               String regionString;
               if ( state != null ) {
                  String[] regionVals = state.get("region");
                  regionString = regionVals[0];
               }
               else {
                  regionString = "junk";
               }
               // We want to draw a region, so we have to force the tool
               // in case it's a cross hair.
               if ( state != null ) {
                  mapState.setCurrentTool(getToolType(new String[] {rc.getViewRegion()}));
               }
               if ( gen != null) {
                  stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
                  stuff.currentMarkerMode = MapGenerator.MODE_XY;
                  setMarkerToolType(mapState, stuff);
               }
               boolean change = 
                  doChangeRegion(gen, state, sessionContext, rc, regionString);
               if ( !change ) {
                  // The menu selection no longer makes sense.
                  // Set it to nonsense so it reverts to the first selection
                  // in the menu.
                  if (state != null ) {
                     state.add("region", new String[] {"Gooblty Gook"});
                  }
               }
            }
            else if ( action.equals("changeView")) {
               // Fix up a few details...
               
               // Always start with the first variable when changing view.
               whvar = new String[1];
               whvar[0] = "1";
               if ( state != null ) {
                  state.add("whvar", whvar);
               }
               mapState.setCurrentVariable(1);

               Log.debug(this, "Changing view...");
               double xl = mapState.getXloAsDouble();
               double xh = mapState.getXhiAsDouble();
               double xm = (xh + xl)/2.0;
               double yl = mapState.getYloAsDouble();
               double yh = mapState.getYhiAsDouble();
               double ym = (yh + yl)/2.0;
               if (getToolType(new String[] {rc.getViewRegion()}).equals("X")) {
                  Log.debug(this, "Setting ylo=yhi="+ym);
                  mapState.setYlo(Double.toString(ym));
                  mapState.setYhi(Double.toString(ym));
                  mapState.setXlo_compare(mapState.getXlo());
                  mapState.setXhi_compare(mapState.getXhi());
                  mapState.setYlo_compare(Double.toString(ym));
                  mapState.setYhi_compare(Double.toString(ym));
                  int cv = mapState.getCurrentVariable();
                  gen.setMarkerIndex(0);
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(), 
                                           mapState.getXhiAsDouble(), 
                                           mapState.getYloAsDouble(), 
                                           mapState.getYhiAsDouble());
                  gen.setMarkerIndex(1);
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(), 
                                           mapState.getXhiAsDouble(), 
                                           mapState.getYloAsDouble(), 
                                           mapState.getYhiAsDouble());
                  gen.setMarkerIndex(cv-1);
                  setStateOfXandY(state, mapState);
               }
               else if (getToolType(new String[] {rc.getViewRegion()}).equals("Y")) {
                  Log.debug(this, "Setting xlo=xhi="+xm);
                  mapState.setXlo(Double.toString(xm));
                  mapState.setXhi(Double.toString(xm));
                  mapState.setXlo_compare(Double.toString(xm));
                  mapState.setXhi_compare(Double.toString(xm));
                  mapState.setYlo_compare(mapState.getYlo());
                  mapState.setYhi_compare(mapState.getYhi());
                  int cv = mapState.getCurrentVariable();
                  gen.setMarkerIndex(0);
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(), 
                                           mapState.getXhiAsDouble(), 
                                           mapState.getYloAsDouble(), 
                                           mapState.getYhiAsDouble());
                  gen.setMarkerIndex(1);
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(), 
                                           mapState.getXhiAsDouble(), 
                                           mapState.getYloAsDouble(), 
                                           mapState.getYhiAsDouble());
                  gen.setMarkerIndex(cv-1);
                  setStateOfXandY(state, mapState);
               }
               else if (getToolType(new String[] {rc.getViewRegion()}).equals("PT")) {
                  mapState.setXlo(Double.toString(xm));
                  mapState.setXhi(Double.toString(xm));
                  mapState.setYlo(Double.toString(ym));
                  mapState.setYhi(Double.toString(ym));
                  mapState.setXlo_compare(Double.toString(xm));
                  mapState.setXhi_compare(Double.toString(xm));
                  mapState.setYlo_compare(Double.toString(ym));
                  mapState.setYhi_compare(Double.toString(ym));
                  int cv = mapState.getCurrentVariable();
                  gen.setMarkerIndex(0);
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(), 
                                           mapState.getXhiAsDouble(), 
                                           mapState.getYloAsDouble(), 
                                           mapState.getYhiAsDouble());
                  gen.setMarkerIndex(1);
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(), 
                                           mapState.getXhiAsDouble(), 
                                           mapState.getYloAsDouble(), 
                                           mapState.getYhiAsDouble());
                  gen.setMarkerIndex(cv-1);
                  setStateOfXandY(state, mapState);
               }
               // Draw the tool with settdomain and set the map command
               // to firstpt to be ready to accept the first click.
               mapState.setCurrentMapCommand("firstpt");
               mArgs.clear();
               mArg = "op=settdomain";
               mArgs.add(mArg);
               mArg="xhi="+mapState.getXhi();
               mArgs.add(mArg);
               mArg="xlo="+mapState.getXlo();
               mArgs.add(mArg);
               mArg="yhi="+mapState.getYhi();
               mArgs.add(mArg);
               mArg="ylo="+mapState.getYlo();
               mArgs.add(mArg);
               mapState.setDelayedMapOp(Utils.join("&", mArgs));
               if ( state != null ) {
                  state.add("action", new String[] {""});
               }
               sessionContext.setMapState(mapState);
            }
            else {
               /*
                * The action was not set and so we are looking to do some work which
                * is being requested via the query string of the URL.  As noted above
                * this is the only way we can extract the user click location in the map
                * from old browsers.
                *
                * Because we are forced to look at the query string for that case,
                * the lazy programmer decided to look at the query string for a all
                * the other cases as well.
                *
                * The end result of this is that the user interface gets a bunch of
                * strange URL's of the form:
                *
                * http://server.com/las/servlets/constrain?mapop=zoomin
                * There is code at the bottom of this method that uses a redirect
                * which causes the browser URL to always have the form:
                *
                * http://server.com/las/servlets/constrain?var=199
                *
                * When the redirect occurs another round trip to the server is
                * initiated.  The second trip through the servlet does essentially
                * no work so will return very quickly.  It is possible that folks
                * will decide that this redirect business is too big a performance hit
                * and want to take it out.  Seem ok to me now, however. 
                *
                */

                Log.debug(this, "Action should not have interesting value"+action);

                String qstring = req.getQueryString(); 
                // The Velocity engine can fire up a Bean during it's initialization
                // without the page being loaded.  If this happens the query string
                // will be null and we just need to return with no harm no foul.
               if (qstring == null || qstring.equals("none")) {

                  Log.debug(this,"The QUERY String WAS NULL.");
                  doStartMap(gen, state, sessionContext, rc);
               }
               else {
                  Log.debug(this,"Operating on this query string: "+qstring);

                  // This is the first case lazily handled.  The page
                  // loads when the URL looks like: 
                  // http://server.com/las/servlets/constrain?var=888
                  
                  if ( qstring.indexOf("var=") >= 0  ) {

                     // This could be first visit to the constraints page for this
                     // variable.

                     doStartMap(gen, state, sessionContext, rc);
        
                  } else if ( qstring.indexOf("reset") >= 0  ) {

                     // Reset the map to the default state.

                     doResetMap(gen, state, sessionContext, rc);

                  } 
                  else if ( qstring.indexOf("firstvar") >= 0  ) {

                     // Get ready to do the first click when we swich back
                     // to the first variable.

                     mapState.setCurrentMapCommand("firstpt"); 

                     // Selecting the first variable; do nothing.
                    
                     mArgs.clear();
                     mArg = "op=none";
                     mArgs.add(mArg);
                     mapState.setDelayedMapOp(Utils.join("&", mArgs));
                     sessionContext.setMapState(mapState);

                  } 
                  else if ( qstring.indexOf("secondvar") >= 0  ) {

                     // Selecting the second variable; set the text boxes.

                     mArgs.clear();
                     mArg = "op=none";
                     mArgs.add(mArg);
                     Rectangle2D tool1 = gen.getMarkerCoordinatesByIndex(0);

                     // The X location of the second tool is same as first tool.
                     if ( tool1 != null && tool.equals("X") ) {
                        mapState.setXlo_compare(format.format(tool1.getX()));
                        mapState.setXhi_compare(format.format(tool1.getX()+
                                                              tool1.getWidth()));
                        mapState.setYlo_compare(format.format(toolRect.getY()));
                        mapState.setYhi_compare(format.format(toolRect.getY()+
                                                              toolRect.getHeight()));
                     }

                     if ( tool1 != null && tool.equals("Y") ) {
                        mapState.setXlo_compare(format.format(toolRect.getX()));
                        mapState.setXhi_compare(format.format(toolRect.getX()+
                                                              toolRect.getWidth()));
                        mapState.setYlo_compare(format.format(tool1.getY()));
                        mapState.setYhi_compare(format.format(tool1.getY()+
                                                              tool1.getHeight()));
                     }

                     mapState.setDelayedMapOp(Utils.join("&", mArgs));
                     sessionContext.setMapState(mapState);

                  } 
                  else if ( qstring.indexOf(",") >= 0 &&
                            qstring.indexOf(",") == qstring.lastIndexOf(",")) {

                     // This is the ugly situation alluded to above where we get the click
                     // location via the ismap.
                     //
                     // A single x,y pixel location has been sent in the query string.  
                     // Either we need to mark the map with the cross hairs of 
                     // the first click or draw the box if it's the second click.

                     doClick( qstring, gen, state, sessionContext, rc); 

                     // Any click on the map when drawing a line for variable
                     // number two in comparison mode is enough information to
                     // draw the line.
                     // Forget everything we just did and draw the second
                     // line where the user clicked.
                     if ( gen.getMarkerMode() == MapGenerator.MODE_2X &&
                          mapState.getCurrentVariable() == 2 ) {

                        // Set to firstpt to be ready for a change of tool.
                        mapState.setCurrentMapCommand("firstpt");

                        // Set the x text boxes to match the first line
                        
                        mapState.setXlo_compare(mapState.getXlo());
                        mapState.setXhi_compare(mapState.getXhi());

                        Point2D click = new Point2D.Double(mapState.getClickX(), 
                                                    mapState.getClickY());
                        Point2D worldclick = gen.userToWorld(click);

                        // Force operation to be settdomain.

                        mArgs.clear();
                        mArg = "op=settdomain";
                        mArgs.add(mArg);
                        mArg = "xlo="+mapState.getXloAsDouble();
                        mArgs.add(mArg);
                        mArg = "xhi="+mapState.getXhiAsDouble();
                        mArgs.add(mArg);
                        mArg = "ylo="+worldclick.getY();
                        mArgs.add(mArg);
                        mArg = "yhi="+worldclick.getY();
                        mArgs.add(mArg);

                        mapState.setYhi_compare(format.format(worldclick.getY()));
                        mapState.setYlo_compare(format.format(worldclick.getY()));

                        mapState.setDelayedMapOp(Utils.join("&", mArgs));
                        sessionContext.setMapState(mapState);

                     }
                     // Similar for MODE_2Y
                     if ( gen.getMarkerMode() == MapGenerator.MODE_2Y &&
                          mapState.getCurrentVariable() == 2 ) {

                        // Set map command firstpt in case 
                        // we change back to single mode
                        mapState.setCurrentMapCommand("firstpt");

                        // Set the y text boxes to match the first line
                        
                        mapState.setYlo_compare(mapState.getYlo());
                        mapState.setYhi_compare(mapState.getYhi());

                        Point2D click = new Point2D.Double(mapState.getClickX(), 
                                                    mapState.getClickY());
                        Point2D worldclick = gen.userToWorld(click);

                        mArgs.clear();
                        mArg = "op=settdomain";
                        mArgs.add(mArg);
                        mArg = "xlo="+worldclick.getX();
                        mArgs.add(mArg);
                        mArg = "xhi="+worldclick.getX();
                        mArgs.add(mArg);
                        mArg = "ylo="+mapState.getYloAsDouble();
                        mArgs.add(mArg);
                        mArg = "yhi="+mapState.getYhiAsDouble();
                        mArgs.add(mArg);

                        mapState.setXhi_compare(format.format(worldclick.getX()));
                        mapState.setXlo_compare(format.format(worldclick.getX()));

                        mapState.setDelayedMapOp(Utils.join("&", mArgs));
                        sessionContext.setMapState(mapState);

                     }

                  }
                  else if ( qstring.indexOf(",") >= 0 &&
                            qstring.indexOf(",") != qstring.lastIndexOf(",")) {

                     // We want to draw a region so we have to force the tool
                     // to be a region in case the user selects "Go" at the
                     // lat/lon text boxes when the tool is a cross hair.
                     if ( state != null ) {
                        mapState.setCurrentTool(getToolType(new String[] {rc.getViewRegion()}));
                     }
                     if ( gen != null) {
                        stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
                        stuff.currentMarkerMode = MapGenerator.MODE_XY;
                        setMarkerToolType(mapState, stuff);
                     }
                     // There's more than one comma, so assume the query string
                     // looks like NNN,NNN,NNN,NNN and call doChangeRegion.
                     // doChangeRegion will check to see if it gets the input
                     // it nedds.
                     boolean change = 
                        doChangeRegion(gen, state, sessionContext, rc, qstring);
                  }
                  else if ( qstring.indexOf("switch") >= 0 ) {

                     // This query string indicates were are switching from the applet
                     // to the java version.  We must initialize the MapGenerator to
                     // get things started.
                     
                     doSwitch(gen, state, sessionContext, rc, stuff);
                  
                  }
                  else {

                  // This is the catch all case.  If the arguement is not one of
                  // of the commands above requires no special processing.
                  // Just set the map operation to be performed next time the map loads.
       
                     Map names = req.getParameterMap();
                     String name;
                     String[] value;
             
                     for ( Iterator i=names.entrySet().iterator(); i.hasNext(); ) {
                        Map.Entry e = (Map.Entry) i.next();
                        name = (String) e.getKey();
                        value = (String[]) e.getValue();
                        mArg = name+"="+value[0];
                        mArgs.add(mArg);
                        // Some commands pass in the values of the text boxes.  Make
                        // sure they get set in the form state.
                        if ( state != null ) {
                           if ( name.equals("xlo") ) {
                              mapState.setXlo(value[0]);
                              state.add("x_lo", new String[] {value[0]});
                           }
                           else if ( name.equals("ylo") ) {
                              mapState.setYlo(value[0]);
                              state.add("y_lo", new String[] {value[0]});
                           }
                           else if ( name.equals("xhi") ) {
                              mapState.setXhi(value[0]);
                              state.add("x_hi", new String[] {value[0]});
                           }
                           else if ( name.equals("yhi") ) {
                              mapState.setYhi(value[0]);
                              state.add("y_hi", new String[] {value[0]});
                           }
                        }
                     }

                     mapState.setDelayedMapOp(Utils.join("&", mArgs));
                     sessionContext.setMapState(mapState);

                  }// End of if then else for different map operations.
               } // end of null query string 
            }// end of else action = changeRegion

            // The magic of this part of the servlet code depends on a bunch
            // of different weird query strings.  Using this redirect trick
            // we can insure that the URL always looks like
            // http://server.com/las/servlets/constrain?var=199
            //

            String query;
            StringBuffer newquery = new StringBuffer("?");
            variables = 
               mSession.getSessionObject().getCompareVariables(0).get("variables");
            if (variables == null || variables.length == 0){
               throw new IdNotFoundException();
            }
            for (int i=0; i < variables.length; ++i){
               newquery.append("var=").append(variables[i]).append("&");
            }
            query = newquery.substring(0,newquery.length()-1);
            Log.debug(this, "redirecting to:"+req.getRequestURI() + query);
            try {
                  Log.debug(this, "Changing redirect to false");
                  mapState.setRedirect(false);
                  resp.sendRedirect(req.getRequestURI() + query);
            } catch (Exception e){
               throw new ServletException(e);
            }
            setStateOfXandY(state, mapState);
         } // redirect == true
         else {
            Log.debug(this, "Changing redirect to true.");
            mapState.setRedirect(true);
         }
         mapState.setValidXlo(rc.getAxisLo("x"));
         mapState.setValidXhi(rc.getAxisHi("x"));
         mapState.setValidYlo(rc.getAxisLo("y"));
         mapState.setValidYhi(rc.getAxisHi("y"));
         mapState.setImageID( rc.getLiveMap().getImageID() );
         sessionContext.setMapState(mapState);
      } // end of if block for !UseJava.

   } // end of init
   public void setMarkerToolType(MapStateBean mapState, 
                                 MapgenBean.MapgenBeanStuff stuff) {

      // If the current tool is a cross that means we're in the middle
      // of a two click operation and we should leave the tool alone
      // unless it's type "pt" which should always be set.

      String toolType = mapState.getCurrentTool(); 

      if ( stuff.gen.getMarkerMode() != MapGenerator.MODE_CROSS ) {
         toolType = toolType.toLowerCase();
         if (toolType.equals("xy")){
            stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
            stuff.currentMarkerMode = MapGenerator.MODE_XY;
         } else if (toolType.equals("x")){
            stuff.gen.setMarkerMode(MapGenerator.MODE_2X);
            stuff.currentMarkerMode = MapGenerator.MODE_2X;
         } else if (toolType.equals("y")){
            stuff.gen.setMarkerMode(MapGenerator.MODE_2Y);
            stuff.currentMarkerMode = MapGenerator.MODE_2Y;
         } else if (toolType.equals("pt")){
            stuff.gen.setMarkerMode(MapGenerator.MODE_2PT);
            stuff.currentMarkerMode = MapGenerator.MODE_2PT;
         } else {
            stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
            stuff.currentMarkerMode = MapGenerator.MODE_XY;
         }
      }
      Log.debug(this, "The view is: "+toolType);
   } // End of setMarkerToolType
      public void setStateOfXandY (FormParameters state, MapStateBean mapState) {
         if ( state != null ) {
            if ( mapState.getCurrentVariable() == 1 ) {
               state.add("x_lo", new String[] {mapState.getXlo()});
               state.add("x_hi", new String[] {mapState.getXhi()});
               state.add("y_lo", new String[] {mapState.getYlo()});
               state.add("y_hi", new String[] {mapState.getYhi()});
            }
            else {
               state.add("x_lo_compare", new String[] {mapState.getXlo_compare()});
               state.add("x_hi_compare", new String[] {mapState.getXhi_compare()});
               state.add("y_lo_compare", new String[] {mapState.getYlo_compare()});
               state.add("y_hi_compare", new String[] {mapState.getYhi_compare()});
            }
         }
      } // end of setStateOfXandY
      public boolean isComparable (String var) throws SQLException, ServletException {
         ICategory cat; 
         try {
           cat = new Category();
           cat.deserialize(var);
         } catch (IdNotFoundException nocate){
           cat = new DerivedCategory();
           cat.deserialize(var);
         }
         catch (SQLException e) {
            throw new 
               ServletException("Can't get info to test isComparable. "+e.getMessage());
         }


         if ( cat.getGridType().equals("regular") ) {
            return true;
         }
         else {
            return false;
         }
      }
}// End of class
