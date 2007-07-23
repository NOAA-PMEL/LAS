// $Id: ConstrainBean.java,v 1.27.2.2 2005/05/19 20:50:35 rhs Exp $
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

public class ConstrainBean extends DefaultTemplateBean {

   // Keep some state information here.

   Vector mArgs = new Vector();
   String mArg;

   String mView;
   boolean mUseMe = true;

   public boolean useTemplate() {
     return mUseMe;
   }

   public void init(TemplateContext tc) throws SQLException, ServletException{
      super.init(tc);
      Log.debug(this,"+=+=+=+=+=+=+=+=+=+=+ Start of ConstrainBean +=+=+=+=+=+=+=+=+=");
      HttpServletRequest req = tc.getServletRequest();
      HttpServletResponse resp = tc.getServletResponse();
      TemplateSession session = Utils.getSession(req);
      SessionTemplateContext sessionContext = session.getSessionContext();

      RegionConstraint rc=null;

      // Do the things needed for both the java and non-java map.
      rc = common(tc);

      String[] variables = req.getParameterValues("var");
      FormParameters sessionParams = mHandler.getVariables();
      if (variables == null){
         variables = sessionParams.get("variables");
      } 
      // Manage the non-java map interface for the single dataset case.
      if ( Utils.getBeanRootFromURI(req.getRequestURI()).indexOf("compare") < 0 ) {
         if ( !sessionContext.getUseJava().booleanValue() ) {

            Log.debug(this,"Firing ConstrainBean!!");
            Log.debug(this,"TemplateRoot="+Utils.getBeanRootFromURI(req.getRequestURI()));

            MapgenBean.MapgenBeanStuff stuff =
                 (MapgenBean.MapgenBeanStuff)sessionContext.get("mapgen");
            MapGenerator gen = null;
            MapStateBean mapState = (MapStateBean)sessionContext.getMapState();
            //Log.debug(this,"redirect = "+mapState.getRedirect());
            if ( mapState.getRedirect() ) {
               //Vector options = options(tc, rc);
               //sessionContext.setOptions(options);
               //Log.debug(this,"Setting options server-side case: "+options.size()+" options");

               if ( stuff != null ) {
                  gen = stuff.gen;
                  // Have to be working on index 0 when in this bean.
                  gen.setMarkerIndex(0);
                  // See if the underlying image has changed
                  // and if it has update it.
               }
               else {
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
               // Make sure this variable allows the current view.
               // If not set it to the first allowed by this variable.
               
               /* It's possible I don't need all this crap.  I
                * just need rc.getViewRegion().  Astounding.

               Vector viewItems = rc.getViewItems();
               String previousView = "";
               String[] stateView=null;
               if ( state != null ) {
                  Log.debug(this," State:\n"+state.toString());
                  //stateView = state.get("view");
                  Log.debug(this," PreviousView:\n"+stateView[0]);
                  //previousView = stateView[0];
                  previousView = rc.getViewRegion();
               }
               boolean isOk = false;
               for (Iterator view = viewItems.iterator(); view.hasNext(); ){
                  View v = (View)view.next();
                  //String val = v.getValue();
                  String val = v.getRegion();
                  if ( val.equals(previousView) ) {
                     isOk = true;
                  }
               }
               String[] theView;
               if (isOk) {
                  theView = stateView;
               } else {
                  View v = (View) viewItems.get(0);
                  theView = new String[] {v.getRegion()};
               }
               */

               // This may be all we need.  !??!?
               String[] theView;
               theView = new String[] {rc.getViewRegion()};

               mapState.setCurrentTool(getToolType(theView));
               if ( gen != null) {
                  setMarkerToolType(mapState, stuff);
               }

               mapState.setNeedTwoVars(false);
               mapState.setCurrentVariable(1);

               /* This Bean is a strange amalgamation of many different techniques for
                * passing and maintaining the state of the user interface.
                *
                * The first difficulty is that there is no convenient way to get the
                * mouse click coordinates in an image from an old browser.  In order for
                * the interface to work with old browser, the ismap construct must be used
                * on the client.  The ismap interface passes the click coordinates on the
                * URL query (the text after the "?") so this Bean must parse and 
                * crack that information rather than use all of the tidy mechanisms 
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
                * ConstrainFormBean and we must perform the requested region change
                * operation in this Bean.  Therefore the first thing we have to check
                * is to see if this Bean was invoked as a result of a change in the Region
                * menu.  If it was, we must deal with that first.
                *
                * Also consider that when the non-java constrain page loads the map image
                * is retrived from the servlet engine everytime.  Therefore it is possible
                * for the servlet to perform a transformation on the map before 
                * sending the image bytes.  This code takes advantage of that fact 
                * and makes some map transformations happen right before the 
                * map is sent to the browser by interpreting a command in 
                * the query string of the request for the image.
                */

               String action="none";
               String[] actions = {"0"};
               if ( state != null ) {
                  actions = state.get("action");
               }
               if (actions != null) {
                  action=actions[0];
               }

               DecimalFormat format = new DecimalFormat("###.####");
               // True if the user pulled down the region menu.
               if ( action.equals("changeRegion") ) {
                  // Get the values for the selected region.
                  String[] regionVals = state.get("region");
                  String regionString = regionVals[0];

                  // Force the tool to be the correct tool for this
                  // view since we're going to draw a region.
                  mapState.setCurrentTool(getToolType(theView));
                  if ( gen != null) {
                     // Set it XY region in case it's a cross hair.
                     stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
                     stuff.currentMarkerMode = MapGenerator.MODE_XY;
                     // Set to to what's appropriate for the view.
                     setMarkerToolType(mapState, stuff);
                  }
                  boolean change = 
                     doChangeRegion(gen, state, sessionContext, rc, regionString);
                  if ( !change ) {
                     // The menu selection no longer makes sense.
                     // Set it to nonsense so it reverts to the first selection
                     // in the menu.
                     if ( state != null ) {
                        state.add("region", new String[] {"Gooblty Gook"});
                     }
                  }
               }
               else if ( action.equals("changeView") ) {
                  // Fix up a few details...
                  
                  double yl = mapState.getYloAsDouble();
                  double yh = mapState.getYhiAsDouble();
                  double ym = (yh + yl)/2.0;
                  double xl = mapState.getXloAsDouble();
                  double xh = mapState.getXhiAsDouble();
                  double xm = (xh + xl)/2.0;

                  //Log.debug(this, "Changing view...");

                  if (getToolType(new String[] {rc.getViewRegion()}).equals("X")) {
                     //Log.debug(this, "Setting ylo=yhi="+ym);
                     mapState.setYlo(Double.toString(ym));
                     mapState.setYhi(Double.toString(ym));
                  }
                  else if (getToolType(new String[] {rc.getViewRegion()}).equals("Y")) {
                     //Log.debug(this, "Setting xlo=xhi="+xm);
                     mapState.setXlo(Double.toString(xm));
                     mapState.setXhi(Double.toString(xm));
                  }
                  else if (getToolType(new String[] {rc.getViewRegion()}).equals("PT")) {
                     mapState.setXlo(Double.toString(xm));
                     mapState.setXhi(Double.toString(xm));
                     mapState.setYlo(Double.toString(ym));
                     mapState.setYhi(Double.toString(ym));
                  }

                  mArgs.clear();
                  mArg = "op=settdomain";
                  mArgs.add(mArg);
                  mArg = "xlo="+mapState.getXlo();
                  mArgs.add(mArg);
                  mArg = "xhi="+mapState.getXhi();
                  mArgs.add(mArg); 
                  mArg = "ylo="+mapState.getYlo();
                  mArgs.add(mArg);
                  mArg = "yhi="+mapState.getYhi();
                  mArgs.add(mArg); 

                  mapState.setDelayedMapOp(Utils.join("&", mArgs));
                    
                  // Be ready to accept the first click.
                  mapState.setCurrentMapCommand("firstpt");
                  if ( state != null ) {
                     state.add("action",new String[] {""});
                  }
                  sessionContext.setMapState(mapState);
               }
               else {
                  /*
                   * The action was not set and so we are looking to do some work which
                   * is being requested via the query string of the URL.  As noted above
                   * this is the only way we can extract the user click 
                   * location in the map from old browsers.
                   *
                   * Because we are forced to look at the query string for that case,
                   * the lazy programmer decided to look at the query string for a all
                   * the other cases as well.
                   *
                   * The end result of this is that the user interface gets a bunch of
                   * strange URL's of the form:
                   *
                   * http://server.com/las/servlets/constrain?mapop=zoomin
                   *
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

                   //Log.debug(this, "Action should not have interesting value"+action);

                   String qstring = req.getQueryString(); 
                   // The Velocity engine can fire up a Bean during it's initialization
                   // without the page being loaded.  If this happens the query string
                   // will be null and we just need to return with no harm no foul.
                  if (qstring == null || qstring.equals("none") ) {

                     //Log.debug(this,"The QUERY String WAS NULL.");
                     // Seems as if we need to fire up the map under this case??
                     // Should be no harm no foul if we do it unneccessarily. 
                     doStartMap(gen, state, sessionContext, rc);
                     /*
                     mArg = "op=none";
                     mArgs.add(mArg);
                     mapState.setDelayedMapOp(Utils.join("&", mArgs));
                     sessionContext.setMapState(mapState);
                     */
                  }
                  else {
                     //Log.debug(this,"Operating on this query string: "+qstring);

                     // This is the first case lazily handled.  The page
                     // loads when the URL looks like: 
                     // http://server.com/las/servlets/constrain?var=888
                     
                     if ( qstring.indexOf("var=") >= 0  ) {

                        // This is first visit to the constraints page for this
                        // variable.

                        Log.debug(this,"Firing startmap for first visit to constrain page this variable.");

                        doStartMap(gen, state, sessionContext, rc);
           
                     } 
                     else if ( qstring.indexOf("reset") >= 0  ) {

                        // Reset the map to the default state.

                        doResetMap(gen, state, sessionContext, rc);

                     } 
                     else if ( qstring.indexOf(",") >= 0 &&
                               qstring.indexOf(",") == qstring.lastIndexOf(",")) {

                        // This is the ugly situation alluded to above where we 
                        // get the click location via the ismap.
                        //
                        // A single x,y pixel location has been sent in the query string.  
                        // Either we need to mark the map with the cross hairs of 
                        // the first click or draw the box if it's the second click.

                        doClick( qstring, gen, state, sessionContext, rc); 

                     }
                     else if ( qstring.indexOf(",") >= 0 &&
                               qstring.indexOf(",") != qstring.lastIndexOf(",")) {

                        // There's more than one comma, so assume the query string
                        // looks like NNN,NNN,NNN,NNN and call doChangeRegion.
                        // doChangeRegion will check to see if it gets the input 
                        // it needs.

                        // We're going to draw a region so force the tool
                        // to match the view in case we're using the cross hair
                        // tool when somebody suddenly decides they want to change
                        // the region.
                        mapState.setCurrentTool(getToolType(theView));
                        if ( gen != null) {
                           // Set it XY region in case it's a cross hair.
                           stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
                           stuff.currentMarkerMode = MapGenerator.MODE_XY;
                           // Set to to what's appropriate for the view.
                           setMarkerToolType(mapState, stuff);
                        }
                        Log.debug(this, "Doing changeRegion on query string."); 
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
                     // Just set the map operation to be performed next 
                     // time the map loads.
          
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

                     }
                  } // End of if then else for different map operations.
               }// end of else action = changeRegion

               // The magic of this part of the servlet code depends on a bunch
               // of different weird query strings.  Using this redirect trick
               // we can insure that the URL always looks like
               // http://server.com/las/servlets/constrain?var=199
               //

               String query;
               StringBuffer newquery = new StringBuffer("?");
               for (int i=0; i < variables.length; ++i){
                  newquery.append("var=").append(variables[i]).append("&");
               }
               query = newquery.substring(0,newquery.length()-1);
               try {
                  resp.sendRedirect(req.getRequestURI() + query);
               } catch (Exception e){
                  throw new ServletException(e);
               }
               //Log.debug(this,"Setting redirect to false.");
               mapState.setRedirect(false);
            }// end of redirect == true;
            else {
               //Log.debug(this,"Setting redirect to true.");
               mapState.setRedirect(true);
            }

            mapState.setImageID( rc.getLiveMap().getImageID() );
            mapState.setValidXlo(rc.getAxisLo("x"));
            mapState.setValidXhi(rc.getAxisHi("x"));
            mapState.setValidYlo(rc.getAxisLo("y"));
            mapState.setValidYhi(rc.getAxisHi("y"));
            sessionContext.setMapState(mapState);
         } // end of else block for UseJava.
         else {
            //Log.debug(this,"USING REDIRECT CODE FOR JAVA MAP!!");
            String query = req.getQueryString();
            if (query == null || query.equals("")){
               StringBuffer newquery = new StringBuffer("?");
               for (int i=0; i < variables.length; ++i){
                  newquery.append("var=").append(variables[i]).append("&");
               }
               query = newquery.substring(0,newquery.length()-1);
               try {
                  resp.sendRedirect(req.getRequestURI() + query);
               } catch (Exception e){
                  throw new ServletException(e);
               }
               mUseMe = false;
            }
            //Vector options = options(tc, rc);
            //Log.debug(this, "Setting options java case. "+options.size()+" options.");
            //sessionContext.setOptions(options);
         }
      }  // end of if not compare template
      Log.debug(this,"+=+=+=+=+=+=+=+=+=+=+ End of ConstrainBean +=+=+=+=+=+=+=+=+=");
   } // end of init
   public RegionConstraint common(TemplateContext tc) 
      throws SQLException, ServletException{ 

      HttpServletRequest req = tc.getServletRequest();
      HttpServletResponse resp = tc.getServletResponse();
      TemplateSession session = Utils.getSession(req);
      SessionTemplateContext sessionContext = session.getSessionContext();
      // Do all the state management required for either the 
      // java or non-java constrain page.
      
      String[] variables = req.getParameterValues("var");
    
      FormParameters sessionParams = mHandler.getVariables();
      if (variables == null){
         variables = sessionParams.get("variables");
      } 

      if (variables == null || variables.length == 0){
         throw new IdNotFoundException();
      }

      // Redirect to URL with all variable parameters in URL so user can
      // use to access constrain page for this variable
      
      RegionConstraint rc=null;


      // Set form parameters
      FormHandler fh = (FormHandler)Utils.getContext(req).get(Constants.FORM);
      FormParameters params =
        (FormParameters)session.getSessionObject().getConstrainState();
      fh.setFormParameters(params);
   
      String view = null;
      if (params != null){
      Log.debug(this,"Form parameters in common: "+params.toString());
         String[] views = params.get("view");
         if (views != null){
            mView = view = views[0];
         }
   
         // Debug stuff
         String output = params.get("output")[0];
         Vector ops = Utils.split("/,/", output);
         String op = (String)ops.elementAt(0);
         Log.debug(this, "Output operations in params list set to:" + op);
         // End of Debug stuff

      }
   
      try {
         rc = RegionConstraint.getInstance(variables, view);
      } catch (Exception idnfe){
         // OK, bad variable id(s). Try the session values, which always should
         // work
         variables = mHandler.getVariables().get("variables");
         rc = RegionConstraint.getInstance(variables, view);
      }
      // Now that we know that the variables are in the db, store whatever 
      // variable parameters we got
      sessionParams.remove("variables");
      sessionParams.add("variables", variables);

      ICategory c = rc.getCategory();

      // Reset constraint parameters if dataset has changed
      RegionConstraint oldrc = mHandler.getRegion();
   
      Log.debug(this,"Looking at old RegionConstraint");
      if (oldrc != null){
	 Log.debug(this,"old RegionConstraint is NOT null");
         ICategory oldc = oldrc.getCategory();
         if (oldc != null){
	    Log.debug(this,"old category is NOT null. old="+oldc.getParentid()+" new="+c.getParentid());
            if (!oldc.getParentid().equals(c.getParentid())){
               FormParameters state =
                  session.getSessionObject().getConstrainState();
               if (state != null){ // Don't save previous constraints
                  for (int i=0; i < Constants.MAX_CONSTRAINTS; ++i){
                     state.remove("constrain" + i + "_apply");
                     state.remove("constrain" + i + "_text");
                  }
                  // Reset the map if data set has changed in case base map is different.
               }
            }
         }
      }

      Vector options = options(tc, rc, params);
      Log.debug(this, "Setting "+options.size()+" options.");
      sessionContext.setOptions(options);

      mHandler.setRegion(rc);
      sessionContext.setLivemap(rc.getLiveMap());
      sessionContext.setCategory(c);
      sessionContext.setCategories(rc.getCategories());
      Vector datasets = DatasetItem.getItems(c.getParentid());
      mHandler.setDatasets(datasets);
      return rc;
   } // End of common()
   public Vector options(TemplateContext tc, RegionConstraint rc, FormParameters params)
      throws SQLException, ServletException { 

      HttpServletRequest req = tc.getServletRequest();
      HttpServletResponse resp = tc.getServletResponse();
      TemplateSession session = Utils.getSession(req);
      String oldTemplate = session.getSessionObject().getLastTemplateName();
      String thisTemplate = getTemplateName();
     
      RegionConstraint oldrc = mHandler.getRegion();
      // If the view has changed, then we need to redo the options menus.


      boolean viewChange=false;
      if ( oldrc != null ) {
         Log.debug(this," View are: "+oldrc.getView()+" != "+rc.getView() );
         if ( !oldrc.getView().equals(rc.getView()) ) {
            Log.debug(this," view changed: "+oldrc.getView()+" != "+rc.getView() );
            viewChange=true; 
         }
      }

      // Either the form has yet to be submitted and thus the value of
      // the output form parameter is not known or we are switching between 
      // comparison and single data set mode.  In either case, use the first
      // item in the list of outputs
      
      if ( oldTemplate == null ) {
         oldTemplate = "";
      }
      // Force the options if changing to constrain from some other template 
      // except in the case of the data template which is just the "reload"
      // after producing an output.
      if  ( (params == null || !oldTemplate.equals(thisTemplate) || viewChange) &&
            (!(oldTemplate.indexOf("data.vm")>=0) &&
             !(oldTemplate.indexOf("data_compare.vm")>=0)) ) {
         Vector ops;
         if ( getTemplateName().indexOf("compare") >= 0) {
            ops = (Vector) rc.getComparisonProducts();
         }
         else {
            ops = (Vector) rc.getProducts();
         }

         Op op = (Op)ops.elementAt(0);
         String op3 = op.getValue();
         if ( params != null ) {
            params.add("output", new String[] {op3});
         }
         Vector operation_parts = Utils.split("/,/", op3);
         String operation = (String)operation_parts.elementAt(0);
         Log.debug(this, "Getting options for op: " + operation + ". First in the list");

         UI ui = rc.getUI();
         Vector options = ui.getOptions(operation);

         return options;
      } 

      else {

         Log.debug(this,"Using existing params");

         // Get the options associated with this operation.
         String output = params.get("output")[0];
         Vector ops = Utils.split("/,/", output);
         String op = (String)ops.elementAt(0);
         Log.debug(this, "Getting options for op:" + op);

         UI ui = rc.getUI();
         Vector options = ui.getOptions(op);
         return options;
      }

   }  // End of options
   public String getToolType(String[] view) {
      String toolType="XY";
      int toolInt = 0;
      for ( int ip=0; ip < view.length; ip++ ) {
         String c = view[ip];
         c = c.toLowerCase();
         Log.debug(this,"view["+ip+"]="+c);
         if ( c.indexOf("x") >= 0) {toolInt += 1;}
         if ( c.indexOf("y") >= 0) {toolInt += 2;}
      }
      if (toolInt == 0){
         toolType = "PT";
      }
      else if (toolInt == 1) {
         toolType = "X";
      }
      else if (toolInt == 2) {
         toolType = "Y";
      }
      else if (toolInt == 3) {
         toolType = "XY";
      }
      return toolType;
   } // End of getToolType
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
            stuff.gen.setMarkerMode(MapGenerator.MODE_X);
            stuff.currentMarkerMode = MapGenerator.MODE_X;
         } else if (toolType.equals("y")){
            stuff.gen.setMarkerMode(MapGenerator.MODE_Y);
            stuff.currentMarkerMode = MapGenerator.MODE_Y;
         } else if (toolType.equals("pt")){
            stuff.gen.setMarkerMode(MapGenerator.MODE_PT);
            stuff.currentMarkerMode = MapGenerator.MODE_PT;
         } else {
            stuff.gen.setMarkerMode(MapGenerator.MODE_XY);
            stuff.currentMarkerMode = MapGenerator.MODE_XY;
         }
      }
      //Log.debug(this, "The view is: "+toolType);
   } // End of setMarkerToolType
   /*
    * This code centers the active part of the globe on the display.
    *
    */
   public void setRestrictedViewWindow(double xlod, double xhid, 
                                       double ylod, double yhid, 
                                       MapGenerator gen, LiveMap livemap) {

      //double xfactor = (360. - (xhid-xlod)) * ((xhid-xlod)/360.); 
      //double yfactor = (180. - (yhid-ylod)) * ((yhid-ylod)/180.);
     
    
      double x1 = Double.valueOf(livemap.getXlo()).doubleValue();
      double x2 = Double.valueOf(livemap.getXhi()).doubleValue();

      double y1 = Double.valueOf(livemap.getYlo()).doubleValue();
      double y2 = Double.valueOf(livemap.getYhi()).doubleValue();

      double xfactor = ( (x2-x1) - (xhid-xlod) ) * ( (xhid-xlod)/(x2-x1) );
      double yfactor = ( (y2-y1) - (yhid-ylod) ) * ( (yhid-ylod)/(y2-y1) );
 

      // If x is more than 10 degrees from the edge 
      // and y is more than 5 degrees of global 
      // set the view window to match the 
      // restricted domain.  
      if ( xfactor > 20.  && yfactor > 10. ) {
         xlod = xlod - xfactor/2.0; 
         xhid = xhid + xfactor/2.0; 
         ylod = ylod - yfactor/2.0; 
         yhid = yhid + yfactor/2.0; 

         // Don't let these go out of range.
	 if ( xlod < -180 ) {
	    xlod = -180;
	 }
	 if ( xlod > 540 ) {
	    xlod = 540;
	 }
	 if ( xhid < -180 ) {
	    xhid = -180;
	 }
	 if ( xhid > 540 ) {
	    xhid = 540;
	 }

	 if (xlod == xhid) {
	    xlod = -180;
	    xhid = 180;
	 }

	 // Don't let these go out of range.
	 if ( ylod < -90 ) {
	    ylod = -90;
	 }
	 if ( ylod > 90 ) {
	    ylod = 90;
	 }
	 if ( yhid < -90 ) {
	    yhid = -90;
	 }
	 if ( yhid > 90 ) {
	    yhid = 90;
	 }

	 if (ylod == yhid) {
	    ylod = -90;
	    yhid = 90;
	 }

         Log.debug(this,"Setting view rectangle to: "+
                              xlod+" : "+xhid+" : "+ylod+" : "+yhid);
         gen.setViewWindow(xlod, xhid, ylod, yhid); 
      } // Otherwise set to it to the entire map.
      else {
         gen.setViewWindow(x1, x2, y1, y2); 
         Log.debug(this,"Setting view rectangle to: "+
                              x1+" : "+x2+" : "+y1+" : "+y2);
      }
   } // end of setRestrictedViewWindow
   public boolean doChangeRegion ( MapGenerator gen, FormParameters state, 
                                SessionTemplateContext sessionContext,
                                RegionConstraint rc, String regionString) 
               throws ServletException {  

            boolean change=false;
            DecimalFormat format = new DecimalFormat("###.####");

            StringTokenizer fields = new StringTokenizer(regionString,",");

            Log.debug(this, "Changing region to: "+regionString);

            MapStateBean mapState = (MapStateBean) sessionContext.getMapState();

            if ( state != null ) {
               state.add("action", new String[] {"none"});
            }

            mArgs.clear();
            mArg="op=none";
            mArgs.add(mArg);
     
            if ( fields.countTokens() < 4 ) {
               throw new ServletException("Fewer than 4 values for tool location");
            }

            double xlod = Double.valueOf(fields.nextToken()).doubleValue();
            double xhid = Double.valueOf(fields.nextToken()).doubleValue();
            double ylod = Double.valueOf(fields.nextToken()).doubleValue();
            double yhid = Double.valueOf(fields.nextToken()).doubleValue();

            if ( xlod > xhid ) {
               xhid = xhid + 360;
            }

            if ( ylod > yhid ) {
               double temp = ylod;
               ylod = yhid;
               yhid = temp;
            }
     
     
            // Is is global in X?
            if ( Math.abs(xlod - xhid) > 360.0 ) {
               mapState.setXlo("-180");
               mapState.setXhi("180");
               xlod = -180;
               xhid = 180;
            } 
     
     
            if ( gen != null ) {
     
               // Zoom out so the box is always visible.
               gen.setZoom(1);
     
               // If data is global in "x" center the view around the box.
               // Otherwise, don't bother.  Do we need to do better?
               
               double xmlo = Double.valueOf(mapState.getValidXlo()).doubleValue();
               double xmhi = Double.valueOf(mapState.getValidXhi()).doubleValue();
               double ymlo = Double.valueOf(mapState.getValidYlo()).doubleValue();
               double ymhi = Double.valueOf(mapState.getValidYhi()).doubleValue();

               gen.setMarkerCoordinates(xlod, xhid, ylod, yhid);

               // If the new marker location intersects with the
               // restricted region, center it and use it.
               if ( gen.getIntersection() ) {

                  change = true;

                  Log.debug(this, "Intersection is true.  Use new marker.");

                  if ( Math.abs(xmhi - xmlo) > 358. ) {

                     Log.debug(this, "Centering on tool marker.");
     
                     double xcenter = 0;
                     double left = -180.;
                     if (xhid > xlod) {
                        xcenter = xlod + Math.abs((xhid - xlod)/2.0);
                     }
                     // Only happens when wrapping around the date line. !?
                     else {
                        /*
                                       (Corner to dateline + dateline to other corner)/2.0 
                        */
                        xcenter = xlod + (180. - xlod       + Math.abs(-180. - xhid))/2.0;
     
                        if ( xcenter > 180. ) {
                           xcenter = xcenter - 360.;
                        }
        
                     }
     
                     if ( xcenter < 0 ) {
                        left = 180. + xcenter;
                     }
                     else {
                        left = -180. + xcenter;
                     }
     
                     double right = left + 360.;

                     Rectangle2D viewWindow = gen.getViewWindow();
     
                     Log.debug(this, "Set window: "+left+" : "+right);
                     gen.setViewWindow( left, right, 
                                        viewWindow.getY(), 
                                        viewWindow.getY()+viewWindow.getHeight());
                  } // End of Map global in X try to center
     
                  // Get the new marker coordinates in case the marker was clipped.
                  Rectangle2D clippedMarker = gen.getMarkerCoordinates();
                  mapState.setXlo(format.format(clippedMarker.getX()));
                  mapState.setXhi(format.format(clippedMarker.getX()+
                                             clippedMarker.getWidth()));
                  mapState.setYlo(format.format(clippedMarker.getY()));
                  mapState.setYhi(format.format(clippedMarker.getY()+
                                                clippedMarker.getHeight()));
               }
               // Otherwise, put the tool back where it was.
               else {
                  Log.debug(this, "Intersection is false.  Use old marker.");
                  change = false;
                  gen.setMarkerCoordinates(mapState.getXloAsDouble(),
                                           mapState.getXhiAsDouble(),
                                           mapState.getYloAsDouble(),
                                           mapState.getYhiAsDouble());
               }

               // We only try to center the box (above) if the data is global in x.
               // If the data is not global, we can restrict the view window. 
               if ( ( xmhi - xmlo ) <= 358. ) {
                  Log.debug(this, "not global data: restrict to: "+xmlo+" "+xmhi+" "+
                                                                   ymlo+" "+ymhi);
                  LiveMap livemap = rc.getLiveMap();
                  setRestrictedViewWindow(xmlo, xmhi, ymlo, ymhi, gen, livemap);
               }
     
            }  // end of if gen != null can center box
     
            // This state variable is contains the state of the Constrain form
            // in the UI.  These values are what gets put in the text boxes.
            if ( state != null ) {
               state.add("x_lo", new String[] {mapState.getXlo()});
               state.add("x_hi", new String[] {mapState.getXhi()});
               state.add("y_lo", new String[] {mapState.getYlo()});
               state.add("y_hi", new String[] {mapState.getYhi()});

            }

            // This action draws a box therefore the next click will be a "firstpt".
            mapState.setCurrentMapCommand("firstpt");

            mapState.setDelayedMapOp(Utils.join("&", mArgs));
            sessionContext.setMapState(mapState);

            return change;

   } // End of doChangeRegion
   public void doStartMap(MapGenerator gen, FormParameters state, 
                          SessionTemplateContext sessionContext, 
                          RegionConstraint rc )  throws ServletException, SQLException { 

      Log.debug(this,"Firing doStartMap method!!"); 

      DecimalFormat format = new DecimalFormat("###.####");

      MapStateBean mapState = (MapStateBean) sessionContext.getMapState();
      LiveMap liveMap = rc.getLiveMap();
      MapgenBean.MapgenBeanStuff stuff =
         (MapgenBean.MapgenBeanStuff)sessionContext.get("mapgen");

      // Should never be null, but you never know.
      if ( gen != null ) {

         Log.debug(this, "Working with existing MapGenerator");

         // Get the location of the current tool.
         int toolindx = mapState.getCurrentVariable();
         toolindx = toolindx - 1;
         Rectangle2D toolRect = gen.getMarkerCoordinatesByIndex(toolindx);


         int zoom = gen.getZoom();

         Rectangle2D viewWindow = gen.getViewWindow();

         // If the domain is not global, set the view port of
         // the map to an area around the restricted domain.
         
         double validXlo = mapState.getValidXloAsDouble();
         double validXhi = mapState.getValidXhiAsDouble();
         double validYlo = mapState.getValidYloAsDouble();
         double validYhi = mapState.getValidYhiAsDouble();

         double xlod = Double.valueOf(rc.getAxisLo("x")).doubleValue(); 
         double xhid = Double.valueOf(rc.getAxisHi("x")).doubleValue(); 
         double ylod = Double.valueOf(rc.getAxisLo("y")).doubleValue(); 
         double yhid = Double.valueOf(rc.getAxisHi("y")).doubleValue();

         double mapXlo = Double.valueOf(liveMap.getXlo()).doubleValue(); 
         double mapXhi = Double.valueOf(liveMap.getXhi()).doubleValue(); 
         double mapYlo = Double.valueOf(liveMap.getYlo()).doubleValue(); 
         double mapYhi = Double.valueOf(liveMap.getYhi()).doubleValue();

         double windowXlo = viewWindow.getX();
         double windowXhi = viewWindow.getX()+viewWindow.getWidth();
         double windowYlo = viewWindow.getY();
         double windowYhi = viewWindow.getY()+viewWindow.getHeight();

         Log.debug(this, "Restricted Domain: "+
               rc.getAxisLo("x")+" "+
               rc.getAxisHi("x")+" "+
               rc.getAxisLo("y")+" "+
               rc.getAxisHi("y")+" ");

         // There are eight possibilities:
         //     1. Same base map, default view window, same valid range
         //          - Restrict domain according to valid range
         //          - center on the valid range 
         //     2. New base map,  default view window, same valid range
         //          - Restrict domain according to valid range
         //          - center on the valid range 
         //     3. Same base map, new view window,     same valid range
         //          - Restrict domain according to valid range
         //          - Set the view window to new view window.
         //     4. New base map,  new view window,     same valid range
         //          - restrict domain
         //          - center on the valid range 
         //     5. Same base map, default view window, new valid range
         //          - restrict domain
         //          - center on the valid range 
         //     6. New base map,  default view window, new valid range
         //          - restrict domain
         //          - center on the valid range 
         //     7. Same base map, new view window,     new valid range
         //          - restrict domain
         //          - center on the valid range; ignore user view window
         //     8. New base map,  new view window,     new valid range
         //          - restrict domain
         //          - center on the valid range; ignore user view window

         if ( liveMap.getImageID() == mapState.getImageID() &&
            // Case 3.
             (Math.abs(mapXlo - windowXlo) > 2.0 ||
              Math.abs(mapXhi - windowXhi) > 2.0 ||
              Math.abs(mapYlo - windowYlo) > 2.0 ||
              Math.abs(mapYhi - windowYhi) > 2.0 ) &&
              Math.abs(validXlo - xlod) <= 2.0 &&
              Math.abs(validXhi - xhid) <= 2.0 &&
              Math.abs(validYlo - ylod) <= 2.0 &&
              Math.abs(validYhi - yhid) <= 2.0 ) {

              Log.debug(this, "doStartMap: case 3");
              gen.setRestrictedCoordinates(xlod, xhid, ylod, yhid);
              gen.setViewWindow(viewWindow);

         }
         else if (liveMap.getImageID() != mapState.getImageID() ){
         // Case 2,4,6,8
              Log.debug(this, "doStartMap: case 2,4,6,8");
              Log.debug(this, "doStartMap: liveMap.getImageID()="+liveMap.getImageID()+"mapState.getImageID()="+mapState.getImageID());
              // Throw away this map generator and start over because
              // there is a new base map.
                  Image image = liveMap.getCurrentImage();
                  byte[] image_data = image.getImage();
                  try {
                     stuff = new MapgenBean.MapgenBeanStuff(
                           new MapGenerator(image_data,320,160));
                     String sxlo = liveMap.getXlo();
                     String sxhi = liveMap.getXhi();
                     String sylo = liveMap.getYlo();
                     String syhi = liveMap.getYhi();
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
                  setMarkerToolType(mapState, stuff);
                  gen = stuff.gen;
              
              gen.setRestrictedCoordinates(xlod, xhid, ylod, yhid);
              setRestrictedViewWindow(xlod, xhid, ylod, yhid, gen, liveMap); 
              sessionContext.put("mapgen",stuff);
         }
         else {
         // Case 1,5,7
              Log.debug(this, "doStartMap: case 1,5,7");
              gen.setRestrictedCoordinates(xlod, xhid, ylod, yhid);
              setRestrictedViewWindow(xlod, xhid, ylod, yhid, gen, liveMap); 
         }
         
         // The tool might have been clipped by the
         // the restricted view window operation above so
         // check its intersection with the current map and view.
         
         // Set the marker coordinates to the old location.
         // If they need to be clipped they will be.
         gen.setMarkerCoordinates(toolRect.getX(), toolRect.getX()+toolRect.getWidth(),
                                  toolRect.getY(), toolRect.getY()+toolRect.getHeight()); 

         // Get them back incase they were clipped.
         toolRect = gen.getMarkerCoordinatesByIndex(toolindx);

            Log.debug(this, "Resetting tool location: "+format.format(toolRect.getX())+
                            " "+format.format(toolRect.getX()+toolRect.getWidth())+
                            " "+format.format(toolRect.getY())+
                            " "+format.format(toolRect.getY()+toolRect.getHeight()));

        
         // Set the text values for the form to the current tool position 
         if ( mapState.getCurrentTool().equals("X") ) { 
            mapState.setXlo(format.format(toolRect.getX())); 
            mapState.setXhi(format.format(toolRect.getX()+toolRect.getWidth()));
            mapState.setYlo(format.format(toolRect.getY()+(toolRect.getHeight()/2.0))); 
            mapState.setYhi(format.format(toolRect.getY()+(toolRect.getHeight()/2.0)));
         }
         else if ( mapState.getCurrentTool().equals("Y") ) { 
            mapState.setXlo(format.format(toolRect.getX()+(toolRect.getWidth()/2.0))); 
            mapState.setXhi(format.format(toolRect.getX()+(toolRect.getWidth()/2.0)));
            mapState.setYlo(format.format(toolRect.getY())); 
            mapState.setYhi(format.format(toolRect.getY()+toolRect.getHeight()));
         }
         else if ( mapState.getCurrentTool().equals("PT") ) { 
            mapState.setXlo(format.format(toolRect.getX())); 
            mapState.setXhi(format.format(toolRect.getX()));
            mapState.setYlo(format.format(toolRect.getY())); 
            mapState.setYhi(format.format(toolRect.getY()));
         }
         else { 
            mapState.setXlo(format.format(toolRect.getX())); 
            mapState.setXhi(format.format(toolRect.getX()+toolRect.getWidth()));
            mapState.setYlo(format.format(toolRect.getY())); 
            mapState.setYhi(format.format(toolRect.getY()+toolRect.getHeight()));
         }


         // We should always just center on the tool. That might save some trouble.
         //
         // centerOnTool(mapState, livemap); 

         // The Marker might have been clipped by the view area.  
         // Since we've been here before (i.e. gen != null) we
         // don't need to restrict the domain as the operation after
         // the constrain page loads (we just did that) so the operation
         // for after the page loads will be to draw the current tool.

         mArgs.clear();
         mArg = "op=none"; 
         mArgs.add(mArg); 
      } 

      if ( state != null ) {
            //Log.debug(this,"set state xlo="+mapState.getXlo());
            //Log.debug(this,"set state xhi="+mapState.getXhi());
            //Log.debug(this,"set state ylo="+mapState.getYlo());
            //Log.debug(this,"set state yhi="+mapState.getYhi());
            state.add("x_lo", new String[] {mapState.getXlo()});
            state.add("x_hi", new String[] {mapState.getXhi()});
            state.add("y_lo", new String[] {mapState.getYlo()});
            state.add("y_hi", new String[] {mapState.getYhi()}); 

      } 

      mapState.setDelayedMapOp(Utils.join("&", mArgs));
      sessionContext.setMapState(mapState);

   } // End of StartMap
   public void doResetMap (MapGenerator gen, FormParameters state, 
                           SessionTemplateContext sessionContext, 
                           RegionConstraint rc) throws SQLException { 

      MapStateBean mapState = (MapStateBean)sessionContext.getMapState();

      // This is the reset operation.  Put the map back
      // to a default state for this variable.

      // Start by setting up the operation to restrict the domain
      // to match this variable.

      mArg = "op=restdomain";
      mArgs.add(mArg); 
      mArg = "xlo="+rc.getAxisLo("x");
      mArgs.add(mArg);
      mArg = "xhi="+rc.getAxisHi("x");
      mArgs.add(mArg);
      mArg = "ylo="+rc.getAxisLo("y");
      mArgs.add(mArg);
      mArg = "yhi="+rc.getAxisHi("y");
      mArgs.add(mArg);

      // Reset the user pan and zoom state and set the 
      // map to reasonable defaults.

      if ( gen != null ) {

         gen.setUserZoom(false); 
         gen.setUserPan(false);
         gen.setZoom(1);

         // Set the tool to cover the whole domain and 
         // set the view window to center any restricted 
         // domain in the center of the map.

         mapState.setXlo(rc.getAxisLo("x")); 
         mapState.setXhi(rc.getAxisHi("x")); 
         mapState.setYlo(rc.getAxisLo("y"));
         mapState.setYhi(rc.getAxisHi("y")); 

         double xlod = Double.valueOf(mapState.getXlo()).doubleValue(); 
         double xhid = Double.valueOf(mapState.getXhi()).doubleValue(); 
         double ylod = Double.valueOf(mapState.getYlo()).doubleValue(); 
         double yhid = Double.valueOf(mapState.getYhi()).doubleValue();

         // Set the tool to the entire extent of the valid region.  
         gen.setMarkerCoordinates(xlod, xhid, ylod, yhid);

         // Set the view window display the restricted area

         LiveMap livemap = rc.getLiveMap();
         setRestrictedViewWindow(xlod, xhid, ylod, yhid, gen, livemap); 

      }

      // Set the text boxes to the domain of the variable.

      if ( state != null ) {
         state.add("x_lo", new String[] {mapState.getXlo()});
         state.add("x_hi", new String[] {mapState.getXhi()});
         state.add("y_lo", new String[] {mapState.getYlo()});
         state.add("y_hi", new String[] {mapState.getYhi()}); 
      }

      // Next click starts a new selection box.  
      mapState.setCurrentMapCommand("firstpt"); 

      mapState.setDelayedMapOp(Utils.join("&", mArgs));
      sessionContext.setMapState(mapState);

   } // End of ResetMap
   public void doClick (String qstring, MapGenerator gen, FormParameters state,
                        SessionTemplateContext sessionContext,
                        RegionConstraint rc) {

      DecimalFormat format = new DecimalFormat("###.####");
      String x = qstring.substring(0, qstring.indexOf(","));
      String y = qstring.substring(qstring.indexOf(",")+1, qstring.length());

      MapStateBean mapState = (MapStateBean)sessionContext.getMapState();

      mapState.setClickX(Integer.valueOf(x).intValue());
      mapState.setClickY(Integer.valueOf(y).intValue());

      /*
       * Check for intersection with current restricted view.
       * If yes, go on, if no, return with no action.
       *
       */


       Point2D myclick = new Point2D.Double(Double.valueOf(x).doubleValue(),
                                          Double.valueOf(y).doubleValue());

       Point2D myworld = gen.userToWorld(myclick);

       Rectangle2D restricted = gen.getRestrictedCoordinates();
       Rectangle2D marker = new Rectangle2D.Double(myworld.getX(), myworld.getY(), 0, 0);

       if ( !gen.intersects(marker, restricted) ) {
          mArgs.clear();
          mArgs.add("op=none");
          mapState.setDelayedMapOp(Utils.join("&", mArgs));
          sessionContext.setMapState(mapState);
          return;
       }

      // If the draw state object is not set, assume it's a first click.
      if ( mapState.getCurrentMapCommand().equals("none") ) { 
         mapState.setCurrentMapCommand("firstpt"); 
      }

      // If it's a point tool always use the setidomain tool.
      if ( mapState.getCurrentTool().equals("PT") ) {
         //Log.debug(this, "Forcing tool to setidomain because tool is PT");
         mapState.setCurrentMapCommand("setidomain");
      }

      //Log.debug(this,"Setting op for delayed operation to "+mapState.getCurrentMapCommand());
      mArg = "op="+mapState.getCurrentMapCommand();
      mArgs.add(mArg);

      if ( mapState.getCurrentMapCommand().equals("firstpt") ) {
         //Log.debug(this, "doing first click");
         // This was a first click.
         // Set the draw state object to draw a box next time.
         mapState.setCurrentMapCommand("setidomain");

         // If possible get the location of the click in world
         // coordinates so we can fill in the text boxes.
         if ( gen != null ) {

            Point2D click = new Point2D.Double(Double.valueOf(x).doubleValue(),
                                                  Double.valueOf(y).doubleValue());

            Point2D world;

            world = gen.userToWorld(click);

            //Log.debug(this,"Cross hairs at "+world+" in 'world'coordinates.");

            // This is a cross hair so xlo=xhi and ylo=yhi 
           

            // We don't care of the state is null to this op.
            //if ( state != null ) {
               //Log.debug(this, "Setting constrain state to: "+world); 
               mapState.setXlo(format.format(world.getX()));
               mapState.setXhi(mapState.getXlo());
               mapState.setYlo(format.format(world.getY())); 
               mapState.setYhi(mapState.getYlo());
            //} 
         } 
      } 
      else {
         // It's not the first click so draw the tool.
         //Log.debug(this, "doing second click");
         if ( gen != null ) {
            Rectangle2D hair = gen.getMarkerCoordinates();

            //Log.debug(this,"Current crosshair loc:"+hair);
            Point2D click = new Point2D.Double(Double.valueOf(x).doubleValue(),
                                               Double.valueOf(y).doubleValue());

            Point2D worldclick, worldhair;

            worldclick = gen.userToWorld(click); 
            worldhair = new Point2D.Double(hair.getX(), hair.getY());

            double x1 = 0;
            double y1 = 0;
            double x2 = 0;
            double y2 = 0;

            //if ( state != null ) {

               // We're drawing a point, use only the click.  
               if ( mapState.getCurrentTool().equals("PT") ) {

                  x2 = worldclick.getX(); 
                  y2 = worldclick.getY();
                  x1 = x2;
                  y1 = y2;

                  //Log.debug(this,"Setting text box to: "+format.format(x2)+" "+
                  //                                       format.format(y2));

                  mapState.setXlo(format.format(x1));
                  mapState.setXhi(format.format(x2));
                  mapState.setYlo(format.format(y1));
                  mapState.setYhi(format.format(y2));
   

               }
               // We're drawing an X-line.  The line will be drawn at
               // the a Y location that is the average of the cross
               // and the click.
               else if ( mapState.getCurrentTool().equals("X") ) {
                   x1 = worldhair.getX(); 
                   x2 = worldclick.getX();
                   y1 = worldhair.getY(); 
                   y2 = worldclick.getY();

                   double ym = (y1+y2)/2.0;

                   mapState.setXlo(format.format(Math.min(x1,x2)));
                   mapState.setXhi(format.format(Math.max(x1,x2)));
                   mapState.setYlo(format.format(ym));
                   mapState.setYhi(format.format(ym));
               }
               // We're drawing a Y-line. The line will be drawn at the X
               // location that is the average of the cross and the click.
               else if ( mapState.getCurrentTool().equals("Y") ) {

                   x1 = worldhair.getX(); 
                   x2 = worldclick.getX();
                   y1 = worldhair.getY(); 
                   y2 = worldclick.getY();

                   double xm = (x1+x2)/2.0;
                   //Log.debug(this, "Drawing Y line at x="+xm+" y1="+y1+" y2="+y2);

                   mapState.setXlo(format.format(xm));
                   mapState.setXhi(format.format(xm));
                   mapState.setYlo(format.format(Math.min(y1,y2)));
                   mapState.setYhi(format.format(Math.max(y1,y2)));
               }
               else {

                   x1 = worldhair.getX(); 
                   x2 = worldclick.getX();
                   y1 = worldhair.getY(); 
                   y2 = worldclick.getY();

                   mapState.setXlo(format.format(Math.min(x1,x2)));
                   mapState.setXhi(format.format(Math.max(x1,x2)));
                   mapState.setYlo(format.format(Math.min(y1,y2)));
                   mapState.setYhi(format.format(Math.max(y1,y2)));

               }
            // Don't care.  } // state != null 
                     
         }

         // Toggle the draw action unless it's a "PT".
         // In that case always draw the little box at the point.
         if ( !mapState.getCurrentTool().equals("PT") ) {
            mapState.setCurrentMapCommand("firstpt");
         }

      }

      // This is the only point where we care of the state object is null.
      if ( state != null ) {
         setStateOfXandY(state, mapState);
      }

      mArg = "x="+x;
      mArgs.add(mArg);
      mArg = "y="+y;
      mArgs.add(mArg);

      mapState.setDelayedMapOp(Utils.join("&", mArgs));
      sessionContext.setMapState(mapState);

   } // end of doClick    
   public void setStateOfXandY (FormParameters state, MapStateBean mapState) {
         state.add("x_lo", new String[] {mapState.getXlo()}); 
         state.add("x_hi", new String[] {mapState.getXhi()}); 
         state.add("y_lo", new String[] {mapState.getYlo()}); 
         state.add("y_hi", new String[] {mapState.getYhi()}); 
   } // end of setStateOfXandY
   public void doSwitch (MapGenerator gen, FormParameters state, 
                         SessionTemplateContext sessionContext, 
                         RegionConstraint rc, MapgenBean.MapgenBeanStuff stuff) 
                         throws SQLException, ServletException {

       MapStateBean mapState = (MapStateBean) sessionContext.getMapState();
      
      // Start a new MapGenerator
      //Log.debug(this,"Switching from Java to Non-java.");
      if ( gen == null ) {
         // Fire up a map generator and add it to the context.
         try {

            LiveMap livemap = sessionContext.getLivemap();
            Image image = livemap.getCurrentImage();
            byte[] image_data = image.getImage();
            stuff = new MapgenBean.MapgenBeanStuff(new MapGenerator(image_data,320,160));

            String sxlo = livemap.getXlo();
            String sxhi = livemap.getXhi();
            String sylo = livemap.getYlo();
            String syhi = livemap.getYhi();
            double xlo = Double.parseDouble(sxlo);
            double xhi = Double.parseDouble(sxhi);
            double ylo = Double.parseDouble(sylo);
            double yhi = Double.parseDouble(syhi);

            stuff.gen.setImageCoordinates(xlo,xhi,ylo,yhi);

         } catch (Exception e){
            throw new ServletException(e.getMessage());
         }
      }

      gen = stuff.gen;

      setMarkerToolType(mapState, stuff);

      // Set the restricted domain.
      double xlod = Double.valueOf(rc.getAxisLo("x")).doubleValue();
      double xhid = Double.valueOf(rc.getAxisHi("x")).doubleValue();
      double ylod = Double.valueOf(rc.getAxisLo("y")).doubleValue();
      double yhid = Double.valueOf(rc.getAxisHi("y")).doubleValue();
    
      gen.setRestrictedCoordinates(xlod, xhid, ylod, yhid);
                  
      // Set the view window display the restricted area
      LiveMap livemap = rc.getLiveMap();
      setRestrictedViewWindow(xlod, xhid, ylod, yhid, gen, livemap);
    
      if ( state != null ) {
         // Replace the tool location values with the info in the text boxes
         // to be used instead of using the Axis coordinates.
         String[] temp;
         temp = state.get("x_lo");
         mapState.setXlo(temp[0]);
         xlod = Double.valueOf(temp[0]).doubleValue();
         temp = state.get("x_hi");
         mapState.setXhi(temp[0]);
         xhid = Double.valueOf(temp[0]).doubleValue();
         temp = state.get("y_lo");
         mapState.setYlo(temp[0]);
         ylod = Double.valueOf(temp[0]).doubleValue();
         temp = state.get("y_hi");
         mapState.setYhi(temp[0]);
         yhid = Double.valueOf(temp[0]).doubleValue();
      }

      gen.setMarkerCoordinates(xlod, xhid, ylod, yhid);
              
      sessionContext.put("mapgen",stuff);

      mapState.setDelayedMapOp(Utils.join("&", mArgs));
      sessionContext.setMapState(mapState);

   } // End of doSwitch
}  // end of class
