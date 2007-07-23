package gov.noaa.pmel.tmap.las.luis.map;

/**
 * <p>Title: LAS Map Browser</p>
 * <p>Description: </p>
 * <p>Copyright:(c) 2003 Joe Sirott</p>
 * @author Joe Sirott
 * @version 1.0
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import gov.noaa.pmel.tmap.las.luis.Log;

public class MapGenerator extends Canvas {
  /**
   * Marker display modes
   */
  public final static int MODE_XY = 0;
  public final static int MODE_X = 1;
  public final static int MODE_Y = 2;
  public final static int MODE_PT = 3;
  public final static int MODE_CROSS = 4;
  public final static int MODE_2PT = 5;
  public final static int MODE_2X = 6;
  public final static int MODE_2Y = 7;

  /**
   * Pan types
   */
  public final static int LEFT = 0;
  public final static int RIGHT = 1;
  public final static int UP = 2;
  public final static int DOWN = 3;

  /**
   * Coordinates of display image
   */
  private Rectangle2D mImageCoordinates =
    new Rectangle2D.Double(-180, -90, 360, 180);
  /**
   * Coordinates of current view window
   */
  private Rectangle2D mViewWindow = (Rectangle2D)mImageCoordinates.clone();


  /**
   * Coordinates of current marker(s)
   * Certain operations require TWO markers therefore mMarkerRect 
   * should be an array.  Those operations requiring only one marker
   * will have to operate on element "0". 
   */
  private Rectangle2D[] mMarkerRect = { (Rectangle2D)mImageCoordinates.clone(),
                                        (Rectangle2D)mImageCoordinates.clone() };

  /**
   * Index of the active marker.
   */
   private int mMarkerIndex=0;

  /**
   * Coordinates of current restricted region. Can be null.
   */
  private Rectangle2D mRestrictedRect;
  /**
   * Display type of marker
   */
  private int mMarkerMode = MODE_XY;
  /**
   * Width of display image
   */
  private int mImageWidth, mImageHeight;
  /**
   * Minimum marker resolution
   */
  private double mMinMarkerSize = 0.0001;

  AffineTransform mFullWorldToUser, mWorldToUser,
      mImageToUser, mWorldToImage, mUserToWorld;

  private BufferedImage mSourceImage, mOutImage, mGraySrcImage;
  private Image mFileImage, mGrayImage;
  private int mDisplayWidth, mDisplayHeight;
  private int mZoomLevel = 1;
  private static final double mZoomFactor = 1.4; // Same as LiveMap applet
  private static final double mPanFactor = 6.0; // As good as any...

  private boolean mUserPan = false;
  private boolean mUserZoom = false;

  private boolean mIntersection = false;

  /**
   * Create a new map generator from a JPEG image file
   * @param imageFile name of the JPEG file
   * @param displayWidth width in pixels of the rendered image
   * @param displayHeight height in pixels of the rendered image
   * @throws IOException
   */
  public MapGenerator(String imageFile, int displayWidth, int displayHeight)
  throws IOException {
    mFileImage = Toolkit.getDefaultToolkit().getImage(imageFile);
    init(imageFile, displayWidth, displayHeight);
  }

  /**
   * Create a new map generator from bytes representing JPEG image data
   * @param data array containing JPEG image
   * @param displayWidth width in pixels of the rendered image
   * @param displayHeight height in pixels of the rendered image
   * @throws IOException
   */
  public MapGenerator(byte[] data, int displayWidth, int displayHeight)
      throws IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(data);
    mFileImage = ImageIO.read(stream);
    mUserPan=false;
    mUserZoom=false;
    init(null, displayWidth, displayHeight);
  }

  private void init(String imageFile, int displayWidth, int displayHeight) throws
      MapGeneratorException {

    Log.debug(this, "Initializing the Mapgenerator.");

    if (displayWidth <= 0){
      throw new MapGeneratorException("Display width must be greater than 0");
    }
    if (displayHeight <= 0){
      throw new MapGeneratorException("Display height must be greater than 0");
    }
    mDisplayWidth = displayWidth; mDisplayHeight = displayHeight;
    setSize(new Dimension(displayWidth, displayHeight));
    mOutImage = new BufferedImage(mDisplayWidth, mDisplayHeight,
                                  BufferedImage.TYPE_INT_RGB);
    Log.debug(this, "Working with media tracker at:"+ System.getProperty("java.io.tmpdir"));
    MediaTracker tracker = new MediaTracker(this);
    Log.debug(this, "Adding images to media tracker.");
    try {
      tracker.addImage(mFileImage, 0);
      tracker.waitForID(0);
    }
    catch ( InterruptedException e ) {}
    if (tracker.isErrorAny()){
      if (imageFile == null)
        throw new MapGeneratorException("Can't load image file " + imageFile);
      else
        throw new MapGeneratorException("Trouble loading byte data");
    }

    ImageFilter filter = new GrayFilter();
    ImageProducer producer = new FilteredImageSource(mFileImage.getSource(),
        filter);
    mGrayImage = Toolkit.getDefaultToolkit().createImage(producer);
    Log.debug(this, "Adding gray image to media tracker.");
    tracker.addImage(mGrayImage, 1);

    try {
      tracker.waitForID(1);
    } catch (InterruptedException e) {}
    if (tracker.isErrorID(1)) {
      throw new MapGeneratorException("Error creating gray image");
    }
    int iw = mFileImage.getWidth(this);
    int ih = mFileImage.getHeight(this);
    mSourceImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
    Graphics2D big = mSourceImage.createGraphics();
    big.drawImage(mFileImage, 0, 0, this);
    mGraySrcImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
    mGraySrcImage.createGraphics().drawImage(mGrayImage,0,0,this);

    // Fit image into display window
    double displayAspect = (double) mDisplayHeight / (double) mDisplayWidth;
    double imageAspect = (double) ih / (double) iw;
    int newheight, newwidth;
    if (imageAspect < displayAspect) {
      newwidth = mDisplayWidth;
      newheight = (int) (imageAspect * newwidth);
    }
    else {
      newheight = mDisplayHeight;
      newwidth = (int) (newheight / imageAspect);
    }
    mImageWidth = newwidth;
    mImageHeight = newheight;

    setupTransforms();
  }

  static void checkCoordinates(double xlo, double xhi, double ylo, double yhi){
    checkCoordinates(xlo,xhi,ylo,yhi,false);
  }

  static void checkCoordinates(double xlo, double xhi, double ylo, double yhi,
                               boolean allowZeroWidth){
    if (xlo < -180){
      throw new MapGeneratorException("xlo must be greater than -180");
    }
    if (xhi > 540){
      throw new MapGeneratorException("xhi must be less than 540");
    }
    if (xlo > xhi){
      throw new MapGeneratorException("xlo must be less than xhi");
    }
    if (ylo > yhi){
      throw new MapGeneratorException("ylo must be less than yhi");
    }
    if (ylo < -90){
      throw new MapGeneratorException("ylo must be greater than -90");
    }
    if (yhi > 90){
      throw new MapGeneratorException("yhi must be less than 90");
    }
    if (!allowZeroWidth){
      if (xlo == xhi){
        throw new MapGeneratorException("xlo cannot be equal to xhi");
      }
      if (ylo == yhi){
        throw new MapGeneratorException("ylo cannot be equal to yhi");
      }
    }
  }

  void checkIntCoordinates(double xlo, double xhi,
                           double ylo, double yhi){
    if (xlo < 0){
      throw new MapGeneratorException("xlo must be greater than 0");
    }
    if (xhi > mImageWidth){
      throw new MapGeneratorException("xhi must be less than " + mImageWidth);
    }
    if (ylo < 0){
      throw new MapGeneratorException("ylo must be greater than 0");
    }
    if (yhi > mImageHeight){
      throw new MapGeneratorException("yhi must be less than " + mImageHeight);
    }
  }

  private Rectangle2D clipToRegion(double zxlo,double zxhi,
                                  double zylo,double zyhi){
    double width = mImageCoordinates.getWidth();
    double height = mImageCoordinates.getHeight();
    double clipxlo = mImageCoordinates.getX();
    double clipxhi = clipxlo + width;
    double clipylo = mImageCoordinates.getY();
    double clipyhi = clipylo + height;

    // Clip to region
    if (isModulo()) {
      clipxhi += 360.0;
      if (zxlo < clipxlo) {
        zxlo += 360.0;
        zxhi += 360.0;
      }
      if (zxhi > clipxhi) {
        zxlo -= 360.0;
        zxhi -= 360.0;
      }
    }
    double deltax, deltay;
    if (zxlo < clipxlo) {
      deltax = clipxlo - zxlo;
      zxlo = clipxlo;
      zxhi += deltax;
    }
    if (zxhi > clipxhi) {
      deltax = clipxhi - zxhi;
      zxhi = clipxhi;
      zxlo += deltax;
    }
    if (zylo < clipylo) {
      deltay = clipylo - zylo;
      zylo = clipylo;
      zyhi += deltay;
    }
    if (zyhi > clipyhi) {
      deltay = clipyhi - zyhi;
      zyhi = clipyhi;
      zylo += deltay;
    }
    return new Rectangle2D.Double(zxlo, zylo, zxhi - zxlo, zyhi - zylo);
  }

  /**
   * Set the coordinates of the restricted area on the map<p>
   * Regions outside the restricted area will be displayed in gray.
   * The region marker will be clipped to the restricted area
   * @param xlo low value of region in world coordinates
   * @param xhi high value of region in world coordinates
   * @param ylo low value of region in world coordinates
   * @param yhi high value of region in world coordinates
   */
  public void setRestrictedCoordinates(double xlo, double xhi,
                                       double ylo, double yhi){
    checkCoordinates(xlo,xhi,ylo,yhi,true);
    mRestrictedRect = new Rectangle2D.Double(xlo,ylo,xhi-xlo,yhi-ylo);
    Rectangle2D newMark = getMarkerRestrictedIntersect();
    if (newMark == null){
      mMarkerRect[mMarkerIndex] = (Rectangle2D)mRestrictedRect.clone();
    } else {
      mMarkerRect[mMarkerIndex] = newMark;
    }
    if (isModulo() && mRestrictedRect.getWidth() == 360.0){
      mRestrictedRect = new Rectangle2D.Double(-10000000, mRestrictedRect.getY(),
                                               20000000, mRestrictedRect.getHeight());
    }
  }

  public Rectangle2D getRestrictedCoordinates() {
     return mRestrictedRect;
  }

  private double getOverlapDelta(Rectangle2D a, Rectangle2D b) {
    return (a.getX() < b.getX())? 360.0 : -360.0;
  }

  static public boolean intersects(Rectangle2D r1, Rectangle2D r2) {
    double x0 = r1.getX();
    double y0 = r1.getY();
    double x = r2.getX();
    double y = r2.getY();
    double w = r2.getWidth();
    double h = r2.getHeight();
    return (x + w >= x0 &&
            y + h >= y0 &&
            x <= x0 + r1.getWidth() &&
            y <= y0 + r1.getHeight());
  }


  private Rectangle2D getMarkerRestrictedIntersect() {
    Log.debug(this, "restricted:" + mRestrictedRect);
    Log.debug(this, "marker:" + mMarkerRect[mMarkerIndex]);
    // Set marker to restricted region
    // Need to check for intersection because Rectangle2D.intersect
    // can return negative width rectangle
    if (mRestrictedRect == null){
      return (Rectangle2D)mMarkerRect[mMarkerIndex].clone();
    }

    Rectangle2D newMarker = (Rectangle2D)mMarkerRect[mMarkerIndex].clone();
    // An empty rectangle (0 width or height) is not considered in Java2D
    // intersection calculations. So we have to do our own intersect check
    if (intersects(mRestrictedRect, mMarkerRect[mMarkerIndex])){
      Rectangle2D.intersect(mRestrictedRect, mMarkerRect[mMarkerIndex], newMarker);
    } else {
      newMarker = null; // Set empty
    }
    Log.debug(this, "post intersect:" + newMarker);
    if (isModulo() && mRestrictedRect.getWidth() < 360.0){
      double deltax = getOverlapDelta(mMarkerRect[mMarkerIndex], mRestrictedRect);
      Rectangle2D shiftedMarkerRect =
          new Rectangle2D.Double(mMarkerRect[mMarkerIndex].getX() + deltax,
                                 mMarkerRect[mMarkerIndex].getY(),
                                 mMarkerRect[mMarkerIndex].getWidth(),
                                 mMarkerRect[mMarkerIndex].getHeight());
      if (intersects(shiftedMarkerRect,mRestrictedRect)){
        Rectangle2D nrect = (Rectangle2D)mRestrictedRect.clone();
        Rectangle2D.intersect(shiftedMarkerRect, mRestrictedRect, nrect);
        Log.debug(this, "setRestrictedCoordinates:mod post int:" + nrect);
        if (newMarker == null){
          newMarker = (Rectangle2D)nrect.clone();
        } else {
          if (newMarker.getMinX() == nrect.getMaxX() ||
              newMarker.getMaxX() == nrect.getMinX()){
            Rectangle2D.union(newMarker, nrect, newMarker);
          } else {
            double x1, x2;
            if (deltax > 0) {
              x1 = newMarker.getMaxX();
              x2 = nrect.getMinX();
            }
            else {
              x1 = nrect.getMaxX();
              x2 = newMarker.getMinX();
            }
            if (x2 < x1) x2 += 360;
            Rectangle2D dest = new Rectangle2D.Double();
            dest.setFrameFromDiagonal(x1, newMarker.getY(),
                                      x2,
                                      newMarker.getY() + newMarker.getHeight());
            newMarker = dest;
          }
          Log.debug(this, "setRestrictedCoordinates:mod post union:" + newMarker);
        }
      }
    }

    mIntersection = true;
    if (newMarker == null){ // No intersection
      mIntersection = false;
      Point2D cpt1 = clipToRestricted(mMarkerRect[mMarkerIndex].getX(), 
                                      mMarkerRect[mMarkerIndex].getY());
      newMarker = new Rectangle2D.Double(cpt1.getX(),cpt1.getY(),
                                         mMinMarkerSize, mMinMarkerSize);
    }
    return newMarker;
  }

  public boolean getIntersection() {
     return mIntersection;
  }

  Rectangle2D rectFromPts(Point2D cpt1, Point2D cpt2){
    double x1 = cpt1.getX() > cpt2.getX() ? cpt2.getX() : cpt1.getX();
    double x2 = cpt1.getX() < cpt2.getX() ? cpt2.getX() : cpt1.getX();
    double y1 = cpt1.getY() > cpt2.getY() ? cpt2.getY() : cpt1.getY();
    double y2 = cpt1.getY() < cpt2.getY() ? cpt2.getY() : cpt1.getY();
    return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
  }

  private Point2D clipToRestricted(double xin, double yin) {
    double x = xin;
    double y = yin;
    double y1 = mRestrictedRect.getMinY();
    double y2 = mRestrictedRect.getMaxY();
    if (y < y1) y = y1;
    if (y > y2) y = y2;

    double x1 = mRestrictedRect.getMinX();
    double x2 = mRestrictedRect.getMaxX();
    double error = 0;
    if (!mRestrictedRect.contains(x,y)){
      if (x < x1) {
        error = x1 - x; x = x1;
      }
      if (x > x2) {
        error = x - x2; x = x2;
      }
    }
    if (isModulo() && error > 0.0){
      double deltax = getOverlapDelta(mMarkerRect[mMarkerIndex], mRestrictedRect);
      double nx = xin + deltax;
      double nerror = 0;
      if (!mRestrictedRect.contains(nx,y)){
        if (nx < x1){
          nerror = x1 - nx; nx = x1;
        }
        if (nx > x2){
          nerror = nx - x2; nx = x2;
        }
      }
      if (nerror < error){
        x = nx - deltax;
      }
    }
    Point2D cpt = new Point2D.Double(x,y);
    return cpt;
  }

  /**
   * Remove any previously set restricted regions
   */
  public void clearRestrictedCoordinates() {
    mRestrictedRect = null;
  }

  /**
   * Pan the current view in the requested direction
   * @param direction pan direction (LEFT,UP,DOWN,RIGHT)
   */
  public void setPan(int direction){
    double xlo = mViewWindow.getX();
    double ylo = mViewWindow.getY();
    double xhi = xlo + mViewWindow.getWidth();
    double yhi = ylo + mViewWindow.getHeight();
    double width = mViewWindow.getWidth();
    double height = mViewWindow.getHeight();
    mUserPan=true;
    switch(direction){
      case LEFT:
        xlo -= width/mPanFactor;
        xhi -= width/mPanFactor;
        break;
      case RIGHT:
        xlo += width/mPanFactor;
        xhi += width/mPanFactor;
        break;
      case DOWN:
        ylo -= height/mPanFactor;
        yhi -= height/mPanFactor;
        break;
      case UP:
        ylo += height/mPanFactor;
        yhi += height/mPanFactor;
       break;
      default:
        throw new MapGeneratorException("Invalid pan direction:" + direction);
    }
    setViewWindow(clipToRegion(xlo,xhi,ylo,yhi));
   }

   /**
    * Zoom in one level
    */
   public void zoomIn() {
    Log.debug(this, "zoomIn enter as mMarkerMode = "+mMarkerMode); 
    mUserZoom = true;
    mZoomLevel += 1;
    setZoom(mZoomLevel);
    //updateImage();
    Log.debug(this, "zoomIn exit as mMarkerMode = "+mMarkerMode); 
  }

  /**
   * Zoom out one level
   */
  public void zoomOut() {
    Log.debug(this, "zoomOut enter as mMarkerMode = "+mMarkerMode); 
    mUserZoom = true;
    if (mZoomLevel == 1){
      return;
    }
    mZoomLevel -= 1;
    setZoom(mZoomLevel);
    //updateImage();
    Log.debug(this, "zoomOut exit as mMarkerMode = "+mMarkerMode); 
  }

  /**
   * Set the zoom to a given level
   * @param level zoom level
   */
  public void setZoom(int level){
    if (level < 1){
      throw new MapGeneratorException("Level must be > 1");
    }
    mZoomLevel = level;
    double centerx = mViewWindow.getX() + mViewWindow.getWidth()/2.0;
    double centery = mViewWindow.getY() + mViewWindow.getHeight()/2.0;
    double zoomScale = 2.0 * Math.pow(mZoomFactor,level-1);
    double width = mImageCoordinates.getWidth();
    double height = mImageCoordinates.getHeight();
    double width2 = width / zoomScale;
    double height2 = height / zoomScale;
    double zxlo = centerx - width2;
    double zxhi = centerx + width2;
    double zylo = centery - height2;
    double zyhi = centery + height2;
    Rectangle2D clipRect= clipToRegion(zxlo,zxhi,zylo,zyhi);
    setViewWindow(clipRect);
  }

  boolean isModulo() {
    return  mImageCoordinates.getWidth() == 360;
  }

  /**
   * Set the display window coordinates
   * @param xlo low value of x in world coordinates
   * @param xhi high value of x in world coordinates
   * @param ylo low value of y in world coordinates
   * @param yhi high value of y in world coordinates
   */
  public void setViewWindow(double xlo, double xhi, double ylo, double yhi){
    checkCoordinates(xlo,xhi,ylo,yhi);
    mViewWindow = new Rectangle2D.Double(xlo,ylo,xhi-xlo,yhi-ylo);
    setupTransforms();
  }

  /**
   * Set the display window coordinates
   * @param rin bounding box of view window in world coordinates
   */
  public void setViewWindow(Rectangle2D rin){
    checkCoordinates(rin.getX(),rin.getX() + rin.getWidth(),
                     rin.getY(),rin.getY() + rin.getHeight());
    mViewWindow = (Rectangle2D)rin.clone();
    setupTransforms();
  }

  /**
   * Set the coordinates for the map image
   * @param xlo low value of x in world coordinates
   * @param xhi high value of x in world coordinates
   * @param ylo low value of y in world coordinates
   * @param yhi high value of y in world coordinates
   */
  public void setImageCoordinates(double xlo, double xhi, double ylo, double yhi){
    checkCoordinates(xlo,xhi,ylo,yhi);
    mImageCoordinates =
      new Rectangle2D.Double(xlo, ylo, xhi - xlo, yhi - ylo);
    mViewWindow = (Rectangle2D)mImageCoordinates.clone();
    setupTransforms();
  }

  /**
   * Set the display mode of the region marker
   * @param mode marker mode
   */
  public void setMarkerMode(int mode){
    if (mode < MODE_XY || mode > MODE_2Y){
      throw new MapGeneratorException("Invalid mode value:" + mode);
    }
    mMarkerMode = mode;
    adjustMarker();
  }

  public int getMarkerMode() {
    return mMarkerMode;
  }

  /**
   * Set the world coordinates of the region marker
   * @param xlo low value of x in world coordinates
   * @param xhi high value of x in world coordinates
   * @param ylo low value of y in world coordinates
   * @param yhi high value of y in world coordinates
   */
  public void setMarkerCoordinates(double xlo, double xhi, double ylo, double yhi){
    if (isModulo()){
      if (xhi < xlo) xhi += 360;
    }
    checkCoordinates(xlo,xhi,ylo,yhi,true);
    Log.debug(this, "coords:" + xlo + ":" + xhi + ":" + ylo + ":" + yhi);
    Rectangle2D oldMark = (Rectangle2D)mMarkerRect[mMarkerIndex].clone();
    mMarkerRect[mMarkerIndex] = new Rectangle2D.Double(xlo,ylo,xhi-xlo,yhi-ylo);
    adjustMarker();
    Rectangle2D newMark = getMarkerRestrictedIntersect();
    if (newMark == null){
      mMarkerRect[mMarkerIndex] = oldMark;
    } else {
      mMarkerRect[mMarkerIndex] = newMark;
    }
    Log.debug(this, "final mMarkerRect:" + mMarkerRect[mMarkerIndex]);
  }

  /**
   * Set the coordinates of the region marker from image coordinates
   * @param xlo low value of x in image coordinates
   * @param xhi high value of x in image coordinates
   * @param ylo low value of y in image coordinates
   * @param yhi high value of y in image coordinates
   */
  public void setMarkerCoordinatesFromImage(double xlo, double xhi,
                                            double ylo, double yhi){
    checkIntCoordinates(xlo,xhi,ylo,yhi);
    Point2D p1 = new Point2D.Double(xlo,ylo);
    Point2D p2 = new Point2D.Double(xhi,yhi);
    Point2D p1out = new Point2D.Double(),p2out = new Point2D.Double();
    mUserToWorld.transform(p1,p1out);
    mUserToWorld.transform(p2,p2out);
    double nxlo = p1out.getX();
    double nwidth = p2out.getX() - p1out.getX();
    double nylo = p1out.getY();
    double nheight = p2out.getY() - p1out.getY();
    if (nwidth < 0){
      nxlo += nwidth;
      nwidth = -nwidth;
    }
    if (nheight < 0){
      nylo += nheight;
      nheight = -nheight;
    }
    Log.debug(this, "xlo,xhi,ylo,yhi" + xlo + ":" + xhi + ":" + ylo + ":" + yhi);
    Log.debug(this, "nxlo,nwidth,nylo,nheight" + nxlo + ":" + nwidth +
             ":" + nylo + ":" + nheight);
    setMarkerCoordinates(nxlo,nxlo+nwidth, nylo,nylo + nheight);
  }

  public void setLastMarkerCoordinateFromImage(double x, double y){
    if (mMarkerMode == MODE_PT || mMarkerMode == MODE_2PT){
      setMarkerCoordinatesFromImage(x,x,y,y);
      return;
    }
    Point2D p1out = new Point2D.Double(mMarkerRect[mMarkerIndex].getX(), mMarkerRect[mMarkerIndex].getY());
    Point2D p2 = new Point2D.Double(x,y);
    Point2D p2out = new Point2D.Double();
    mUserToWorld.transform(p2,p2out);
    
    // All we need is the Y from this click.
    if ( mMarkerIndex == 1 && mMarkerMode == MODE_2X ) {
       setMarkerCoordinates(
             mMarkerRect[mMarkerIndex].getX(),
             mMarkerRect[mMarkerIndex].getX() + mMarkerRect[mMarkerIndex].getWidth(),
             p2out.getY(),
             p2out.getY());
       return;
    }
    
    // All we need is the X from this click.
    if ( mMarkerIndex == 1 && mMarkerMode == MODE_2Y ) {
       setMarkerCoordinates(
             p2out.getX(),
             p2out.getX(),
             mMarkerRect[mMarkerIndex].getY(),
             mMarkerRect[mMarkerIndex].getY() + mMarkerRect[mMarkerIndex].getHeight());
       return;
    }

    if (mMarkerMode == MODE_X){
      p2out.setLocation(p2out.getX(), mMarkerRect[mMarkerIndex].getY());
    }
    if (mMarkerMode == MODE_Y){
      p2out.setLocation(mMarkerRect[mMarkerIndex].getX(), p2out.getY());
    }
    double nxlo = p1out.getX();
    double nwidth = p2out.getX() - p1out.getX();
    double nylo = p1out.getY();
    double nheight = p2out.getY() - p1out.getY();
    if (nwidth < 0){
      nxlo += nwidth;
      nwidth = -nwidth;
    }
    if (nheight < 0){
      nylo += nheight;
      nheight = -nheight;
    }
    Log.debug(this, "Setting new rectangle:" + nxlo + ":" +
             nxlo + nwidth + ":" + nylo + ":" + nylo + nheight);
    setMarkerCoordinates(nxlo,nxlo+nwidth, nylo,nylo + nheight);
  }

  public Rectangle2D getMarkerCoordinatesFromImage() {
    Point2D p1 = new Point2D.Double(mMarkerRect[mMarkerIndex].getX(),mMarkerRect[mMarkerIndex].getY());
    Point2D p2 = new Point2D.Double(mMarkerRect[mMarkerIndex].getX() + mMarkerRect[mMarkerIndex].getWidth(),
                                    mMarkerRect[mMarkerIndex].getY() + mMarkerRect[mMarkerIndex].getHeight());
    Point2D p1out = new Point2D.Double(),p2out = new Point2D.Double();
    mWorldToUser.transform(p1,p1out);
    mWorldToUser.transform(p2,p2out);
    double nxlo = p1out.getX();
    double nwidth = p2out.getX() - p1out.getX();
    double nylo = p1out.getY();
    double nheight = p2out.getY() - p1out.getY();
    if (nwidth < 0){
      nxlo += nwidth;
      nwidth = -nwidth;
    }
    if (nheight < 0){
      nylo += nheight;
      nheight = -nheight;
    }
    return new Rectangle2D.Double(nxlo,nylo,nwidth,nheight);

  }

  public void paint(Graphics g) {
    Graphics2D g2 = mOutImage.createGraphics();
    genImage(g2);
    Graphics2D gDisplay = (Graphics2D)g;
    gDisplay.drawImage(mOutImage,null,0,0);

  }

  private void adjustMarker() {
    boolean checkx = false, checky = false;
    if (mMarkerMode == MODE_XY){
      checkx = true; checky = true;
    } else if (mMarkerMode == MODE_X || mMarkerMode == MODE_2X){
      checkx = true;
    } else if (mMarkerMode == MODE_Y || mMarkerMode == MODE_2Y){
      checky = true;
    } else if (mMarkerMode == MODE_PT || mMarkerMode == MODE_CROSS || 
               mMarkerMode == MODE_2PT){ // Nada
    } else {
      throw new MapGeneratorException("adjustMarker(): invalid marker mode");
    }
    if (checkx){
      if (mMarkerRect[mMarkerIndex].getWidth() < mMinMarkerSize){
        mMarkerRect[mMarkerIndex].setFrame(mMarkerRect[mMarkerIndex].getX(), mMarkerRect[mMarkerIndex].getY(),
                             mMinMarkerSize, mMarkerRect[mMarkerIndex].getHeight());
      }
    }
    if (checky){
      if (mMarkerRect[mMarkerIndex].getHeight() < mMinMarkerSize){
        mMarkerRect[mMarkerIndex].setFrame(mMarkerRect[mMarkerIndex].getX(), mMarkerRect[mMarkerIndex].getY(),
                             mMarkerRect[mMarkerIndex].getWidth(), mMinMarkerSize);
      }
    }
  }

  private static Rectangle2D transformRectangle(Rectangle2D rin,
                                                AffineTransform t){
    double pts[] =
        new double[] {rin.getX(),rin.getY(),rin.getX() + rin.getWidth(),
                      rin.getY() + rin.getHeight()};
    double dst_pts[] = new double[pts.length];
    t.transform(pts, 0, dst_pts, 0, 2);
    double x = (dst_pts[0] < dst_pts[2]) ? dst_pts[0] : dst_pts[2];
    double y = (dst_pts[1] < dst_pts[3]) ? dst_pts[1] : dst_pts[3];
    double width = Math.abs(dst_pts[2] - dst_pts[0]);
    double height = Math.abs(dst_pts[3] - dst_pts[1]);
    return new Rectangle2D.Double(x,y,width,height);
  }

  private static Line2D transformLine(Line2D lin, AffineTransform t){
    double pts[] =
        new double[] {lin.getX1(),lin.getY1(),
                      lin.getX2(),lin.getY2()};
    double dst_pts[] = new double[pts.length];
    t.transform(pts, 0, dst_pts, 0, 2);
    return new Line2D.Double(dst_pts[0],dst_pts[1],dst_pts[2],dst_pts[3]);
  }

  private static Point2D transformPoint(Point2D pin, AffineTransform t){
    Point2D pout = new Point2D.Double();
    t.transform(pin, pout);
    return pout;
  }

  void genImage(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);

    int iw = mFileImage.getWidth(this);
    int ih = mFileImage.getHeight(this);
    // Transform image to user coords
    g2.setTransform(mImageToUser);
    //Log.debug(this, "normal imageToUser:" + mImageToUser);

    // Draw underlying gray image(s)
    g2.drawImage(mGraySrcImage, null, 0, 0);
    if (isModulo()){
      mImageToUser.translate(iw,0);
      g2.setTransform(mImageToUser);
      g2.drawImage(mGraySrcImage, null, 0, 0);
      mImageToUser.translate(-iw,0);
    }

    drawMapImage(g2,0,mImageToUser, mWorldToImage);

   // Transform image to user coords for wrapped longitude
    if (isModulo()) {
      drawMapImage(g2, iw, mImageToUser, mWorldToImage);
    }
    // Draw marker
    g2.setTransform(new AffineTransform()); // Identity transform
    //g2.setColor(Color.WHITE);
    Color dirtyYellow = new Color(240,230,1);
    Color red = new Color(200, 40, 40);
    g2.setColor(dirtyYellow);
    g2.setStroke(new BasicStroke(3));
    double deltax = getOverlapDelta(mMarkerRect[mMarkerIndex], mViewWindow);
    if (mMarkerMode == MODE_XY){
        Log.debug(this, "genImage: drawing marker rect:" + mMarkerRect[mMarkerIndex]);
        g2.draw(transformRectangle(mMarkerRect[mMarkerIndex], mWorldToUser));
        if (isModulo()) {
          Rectangle2D newrect =
              new Rectangle2D.Double(mMarkerRect[mMarkerIndex].getX() + deltax,
                                     mMarkerRect[mMarkerIndex].getY(),
                                     mMarkerRect[mMarkerIndex].getWidth(),
                                     mMarkerRect[mMarkerIndex].getHeight());
          g2.draw(transformRectangle(newrect, mWorldToUser));
        }
    } else if (mMarkerMode == MODE_X){
        double y = mMarkerRect[mMarkerIndex].getY() + mMarkerRect[mMarkerIndex].getHeight()/2.0;
        Line2D line = new Line2D.Double(mMarkerRect[mMarkerIndex].getX(), y,
                                   mMarkerRect[mMarkerIndex].getX() + mMarkerRect[mMarkerIndex].getWidth(),
                                   y);
        Log.debug(this, "genImage: drawing x line:"+line.getX1()+" "+line.getY1()+" "+
                                                    line.getX2()+" "+line.getY2());
        g2.draw(transformLine(line, mWorldToUser));
        if (isModulo()) {
          line =
              new Line2D.Double(mMarkerRect[mMarkerIndex].getX() + deltax,
                                y,
                                mMarkerRect[mMarkerIndex].getX() + mMarkerRect[mMarkerIndex].getWidth()+deltax,
                                y);
          g2.draw(transformLine(line, mWorldToUser));
        }
    } else if (mMarkerMode == MODE_2X){
        double y = mMarkerRect[0].getY() + mMarkerRect[0].getHeight()/2.0;
        Line2D line = new Line2D.Double(mMarkerRect[0].getX(), y,
                                   mMarkerRect[0].getX() + mMarkerRect[0].getWidth(),
                                   y);
        Log.debug(this, "genImage: drawing x line:"+line.getX1()+" "+line.getY1()+" "+
                                                    line.getX2()+" "+line.getY2());
        g2.draw(transformLine(line, mWorldToUser));
        if (isModulo()) {
          line =
              new Line2D.Double(mMarkerRect[0].getX() + deltax,
                                y,
                                mMarkerRect[0].getX() + mMarkerRect[0].getWidth()+deltax,
                                y);
          g2.draw(transformLine(line, mWorldToUser));
        }
        double y2 = mMarkerRect[1].getY() + mMarkerRect[1].getHeight()/2.0;
               line = new Line2D.Double(mMarkerRect[0].getX(), y2,
                                   mMarkerRect[0].getX() + mMarkerRect[0].getWidth(),
                                   y2);
        Log.debug(this, "genImage: drawing x line:"+line.getX1()+" "+line.getY1()+" "+
                                                    line.getX2()+" "+line.getY2());
        g2.setColor(Color.WHITE);
        g2.draw(transformLine(line, mWorldToUser));
        g2.setColor(dirtyYellow);
        if (isModulo()) {
          line =
              new Line2D.Double(mMarkerRect[0].getX() + deltax,
                                y2,
                                mMarkerRect[0].getX() + mMarkerRect[0].getWidth()+deltax,
                                y2);
          g2.setColor(Color.WHITE);
          g2.draw(transformLine(line, mWorldToUser));
          g2.setColor(dirtyYellow);
        }
    } else if (mMarkerMode == MODE_Y){
      double x = mMarkerRect[0].getX() + mMarkerRect[0].getWidth()/2.0;
      Line2D line = new Line2D.Double(x, mMarkerRect[0].getY(),
                                      x,
                                      mMarkerRect[0].getY() + mMarkerRect[0].getHeight());
      Log.debug(this, "genImage: drawing y line:"+line.getX1()+" "+line.getY1()+" "+
                                                  line.getX2()+" "+line.getY2());
      g2.draw(transformLine(line, mWorldToUser));
      if (isModulo()) {
        x += deltax;
        line = new Line2D.Double(x, mMarkerRect[0].getY(),
                                 x,
                                 mMarkerRect[0].getY() + mMarkerRect[0].getHeight());
        g2.draw(transformLine(line, mWorldToUser));
      }
    } else if (mMarkerMode == MODE_2Y){
      double x = mMarkerRect[0].getX() + mMarkerRect[0].getWidth()/2.0;
      Line2D line = new Line2D.Double(x, mMarkerRect[0].getY(),
                                      x,
                                      mMarkerRect[0].getY() + mMarkerRect[0].getHeight());
      Log.debug(this, "genImage: drawing y line:"+line.getX1()+" "+line.getY1()+" "+
                                                  line.getX2()+" "+line.getY2());
      g2.draw(transformLine(line, mWorldToUser));
      if (isModulo()) {
        x += deltax;
        line = new Line2D.Double(x, mMarkerRect[0].getY(),
                                 x,
                                 mMarkerRect[0].getY() + mMarkerRect[0].getHeight());
        g2.draw(transformLine(line, mWorldToUser));
      }
      x = mMarkerRect[1].getX() + mMarkerRect[1].getWidth()/2.0;
      line = new Line2D.Double(x, mMarkerRect[0].getY(),
                                      x,
                                      mMarkerRect[0].getY() + mMarkerRect[0].getHeight());
      Log.debug(this, "genImage: drawing y line:"+line.getX1()+" "+line.getY1()+" "+
                                                  line.getX2()+" "+line.getY2());
      g2.setColor(Color.WHITE);
      g2.draw(transformLine(line, mWorldToUser));
      g2.setColor(dirtyYellow);
      if (isModulo()) {
        x += deltax;
        line = new Line2D.Double(x, mMarkerRect[0].getY(),
                                 x,
                                 mMarkerRect[0].getY() + mMarkerRect[0].getHeight());
        g2.setColor(Color.WHITE);
        g2.draw(transformLine(line, mWorldToUser));
        g2.setColor(dirtyYellow);
      }
    } else if (mMarkerMode == MODE_PT){
      double x = mMarkerRect[mMarkerIndex].getX();
      double y = mMarkerRect[mMarkerIndex].getY();
      Point2D pt = new Point2D.Double(x,y);
      Point2D pout = transformPoint(pt, mWorldToUser);
      Rectangle2D rect = new Rectangle2D.Double(pout.getX()-3,
                                                pout.getY()-3,
                                                6,6);
      Log.debug(this, "genImage: point box at rect" + rect);
      g2.draw(rect);
      if (isModulo()) {
        x += deltax;
        pt = new Point2D.Double(x,y);
        pout = transformPoint(pt, mWorldToUser);
        rect = new Rectangle2D.Double(pout.getX()-3,
                                      pout.getY()-3,
                                      6,6);
        g2.draw(rect);
      }
    } else if (mMarkerMode == MODE_2PT){
      double x1 = mMarkerRect[0].getX();
      double y1 = mMarkerRect[0].getY();
      double x2 = mMarkerRect[1].getX();
      double y2 = mMarkerRect[1].getY();
      Point2D pt1 = new Point2D.Double(x1,y1);
      Point2D pout1 = transformPoint(pt1, mWorldToUser);
      Rectangle2D rect1 = new Rectangle2D.Double(pout1.getX()-3,
                                                 pout1.getY()-3,
                                                 6,6);
      Log.debug(this, "genImage: POINT 1: point box at rect" + rect1);
      Point2D pt2 = new Point2D.Double(x2,y2);
      Point2D pout2 = transformPoint(pt2, mWorldToUser);
      Rectangle2D rect2 = new Rectangle2D.Double(pout2.getX()-3,
                                                 pout2.getY()-3,
                                                 6,6);
      Log.debug(this, "genImage: POINT 2: point box at rect" + rect2);
      g2.draw(rect1);
      g2.setColor(Color.WHITE);
      g2.draw(rect2);
      g2.setColor(dirtyYellow);
      if (isModulo()) {
        x1 += deltax;
        pt1 = new Point2D.Double(x1,y1);
        pout1 = transformPoint(pt1, mWorldToUser);
        rect1 = new Rectangle2D.Double(pout1.getX()-3,
                                       pout1.getY()-3,
                                       6,6);
        x2 += deltax;
        pt2 = new Point2D.Double(x2,y2);
        pout2 = transformPoint(pt2, mWorldToUser);
        rect2 = new Rectangle2D.Double(pout2.getX()-3,
                                       pout2.getY()-3,
                                       6,6);
        Log.debug(this, "genImage: POINT 1: isModulo: point box at rect" + rect1);
        Log.debug(this, "genImage: POINT 2: isModulo: point box at rect" + rect2);
        g2.draw(rect1);
        g2.setColor(Color.WHITE);
        g2.draw(rect2);
        g2.setColor(dirtyYellow);
      }
    } else if (mMarkerMode == MODE_CROSS){
      int crossLength = 10;
      double x = mMarkerRect[mMarkerIndex].getX();
      double y = mMarkerRect[mMarkerIndex].getY();
      Point2D pt = new Point2D.Double(x,y);
      Point2D pout = transformPoint(pt, mWorldToUser);
      Line2D line1 = new Line2D.Double(pout.getX()-crossLength,pout.getY(),
                                       pout.getX()+crossLength,pout.getY());
      Line2D line2 = new Line2D.Double(pout.getX(),pout.getY()-crossLength,
                                       pout.getX(),pout.getY()+crossLength);
      Log.debug(this, "genImage: cross at " + line1 + " crossed with "+line2);
      g2.draw(line1);
      g2.draw(line2);
      if (isModulo()) {
        x += deltax;
        pt = new Point2D.Double(x,y);
        pout = transformPoint(pt, mWorldToUser);
        line1 = new Line2D.Double(pout.getX()-crossLength,pout.getY(),
                                         pout.getX()+crossLength,pout.getY());
        line2 = new Line2D.Double(pout.getX(),pout.getY()-crossLength,
                                         pout.getX(),pout.getY()+crossLength);
        g2.draw(line1);
        g2.draw(line2);
     }
    } else {
      throw new MapGeneratorException("Unknown marker mode:" + mMarkerMode);
    }
  }

  private void setupTransforms() throws MapGeneratorException {
    int iw = mFileImage.getWidth(this);
    int ih = mFileImage.getHeight(this);

    // Set up coordinate systems. Following coordinate systems:
    //    World coordinates: the lat/lon coordinate system
    //    User coordinates: the coordinate system of the display window (or
    //                      output image)
    //    Image coordinates: the coordinate system of the source image before
    //                      scaling into display window (or output image)

    // Set transform from full world coordinates to image coordinates
    double sx = (double) mImageWidth / mImageCoordinates.getWidth();
    double sy = (double) mImageHeight / mImageCoordinates.getHeight();
    mFullWorldToUser =
        new AffineTransform(sx, 0,
                            0, -sy,
                            -mImageCoordinates.getX() * sx,
                            (mImageCoordinates.getY() +
                             mImageCoordinates.getHeight()) * sy);

    // Get full world to user transform of view window
    Point2D tpt = new Point2D.Double();
    mFullWorldToUser.transform(new Point2D.Double(mViewWindow.getX(),
                                    mViewWindow.getY() + mViewWindow.getHeight()),
                 tpt);


    // World to user transform
    double markersx = (double) mImageWidth / mViewWindow.getWidth();
    double markersy = (double) mImageHeight / mViewWindow.getHeight();
    mWorldToUser =
        new AffineTransform(markersx, 0,
                            0, -markersy,
                            -mViewWindow.getX() * markersx,
                            (mViewWindow.getY() +
                             mViewWindow.getHeight()) * markersy);

    // Image to user transform
    double wsx = mImageCoordinates.getWidth()/mViewWindow.getWidth();
    double wsy = mImageCoordinates.getHeight()/mViewWindow.getHeight();
    mImageToUser = new AffineTransform();
    mImageToUser.scale(wsx,wsy);
    mImageToUser.translate(-tpt.getX(), -tpt.getY());
    mImageToUser.scale( (double) mImageWidth / (double) iw,
                       (double) mImageHeight / (double) ih);

    // World to image transform
    try {
      mWorldToImage = mImageToUser.createInverse();
    }
    catch (NoninvertibleTransformException ex) {
      throw new MapGeneratorException(ex.getMessage());
    }
    mWorldToImage.concatenate(mWorldToUser);
    //Log.debug(this, "worldToImage:" + mWorldToImage);

    // And, finally, user to world transform
    try {
      mUserToWorld = mWorldToUser.createInverse();
    }
    catch (NoninvertibleTransformException ex) {
      throw new MapGeneratorException(ex.getMessage());
    }

  }

  private void drawMapImage(Graphics2D g2, int iw,
                                      AffineTransform imageToUser,
                                      AffineTransform worldToImage) {
  // Draw legal region
  // Note: need to set the clip region AFTER the appropriate units
  // are set through setTransform. If order is reversed, then graphics
  // context transforms the clip coordinates when the transform is applied
  // Documentation is unclear on this...
    AffineTransform newImageToUser = (AffineTransform)imageToUser.clone();
    newImageToUser.translate(iw, 0);
    g2.setTransform(new AffineTransform());
    Rectangle2D[] legalBounds = null;
    if (mRestrictedRect != null) {
      legalBounds = getLegalBounds(worldToImage);
    }
    int length = legalBounds == null ? 1 : legalBounds.length;
    for (int i=0; i < length; ++i){
      g2.setTransform(newImageToUser);
      if (legalBounds != null){
        g2.setClip(legalBounds[i]);
        Log.debug(this, "legalBounds:" + legalBounds[i]);
      }
      g2.drawImage(mSourceImage, null, 0, 0);
      Log.debug(this, "iw:" + iw);
      Log.debug(this, "imageToUser:" + imageToUser);
      Log.debug(this, "clip is:" + g2.getClip());
      g2.setClip(null);
    }
  }

  private Rectangle2D[] getLegalBounds(AffineTransform worldToImage) {
    Rectangle2D rval[] = new Rectangle2D.Double[2];
    Point2D grayHi;
    Point2D grayLow;
    Rectangle2D legalBounds;
    double xlo = mRestrictedRect.getX();
    double ylo = mRestrictedRect.getY();
    double xhi = mRestrictedRect.getX() + mRestrictedRect.getWidth();
    double yhi = mRestrictedRect.getY() + mRestrictedRect.getHeight();
    double xdelta = xlo > mImageCoordinates.getX() ? -360 : 360;

    grayLow = new Point2D.Double(xlo,ylo);
    grayHi = new Point2D.Double(xhi,yhi);
    worldToImage.transform(grayLow,
                           grayLow);
    worldToImage.transform(grayHi,
                           grayHi);
    legalBounds = new Rectangle2D.Double(grayLow.getX(), grayHi.getY(),
                                         grayHi.getX() - grayLow.getX(),
                                         grayLow.getY() - grayHi.getY());
    rval[0] = legalBounds;

    grayLow = new Point2D.Double(xlo + xdelta,ylo);
    grayHi = new Point2D.Double(xhi + xdelta ,yhi);
    worldToImage.transform(grayLow,
                           grayLow);
    worldToImage.transform(grayHi,
                           grayHi);
    legalBounds = new Rectangle2D.Double(grayLow.getX(), grayHi.getY(),
                                         grayHi.getX() - grayLow.getX(),
                                         grayLow.getY() - grayHi.getY());
    rval[1] = legalBounds;

    return rval;
  }

  /**
   * Redraw the map image.
   */
  public void updateImage() {
    Graphics2D g2 = mOutImage.createGraphics();
    genImage(g2);
  }

  /**
   * Write the map image to a output stream
   * @param out OutputStream for image
   * @throws IOException
   */
  public void write(OutputStream out) throws IOException {
    updateImage();
    ImageIO.write(mOutImage, "jpeg", out);
    out.flush();
  }

  /**
   * Write the map image to an output file
   * @param fileName name of output file
   * @throws IOException
   */
  public void write(String fileName) throws IOException {
    updateImage();
    File f = new File(fileName);
    ImageIO.write(mOutImage, "jpeg", f);
  }

  /**
   * Get the current marker coordinates
   * @return world coordinates of marker
   */
  public Rectangle2D getMarkerCoordinates() {
    return (Rectangle2D)mMarkerRect[0].clone();
  }

  /**
   * Get the marker coordinates of the index
   * @return world coordinates of marker
   */
  public Rectangle2D getMarkerCoordinatesByIndex(int index) {
    return (Rectangle2D)mMarkerRect[index].clone();
  }

  /**
   * Translate a point in image space to a point in world space.
   * @ param point in image space
   * @ return wpoint in world space
   */
  public Point2D userToWorld (Point2D point) {
     Point2D world = new Point2D.Double();
     mUserToWorld.transform(point,world);
     return world;
  }

  /**
   * Get the current zoom level
   * @ return zoom
   */
  public int getZoom() {
     return mZoomLevel;
  }

  /**
   * Get the current view window
   * @ return Rectangle2D
   */
   public Rectangle2D getViewWindow() {
      return mViewWindow;
   }

  /**
   * Get the current pan state
   * @ return state
   *
   */
   public boolean getUserPan() {
      return mUserPan;
   }

  /**
   * Set the current pan state
   * @ param state
   *
   */
   public void setUserPan(boolean state) {
      this.mUserPan = state;
   }
  
  /**
   * Get the current zoom state
   * @ return state
   *
   */
   public boolean getUserZoom() {
      return mUserZoom;
   }

  /**
   * Set the current zoom state
   * @ param state
   *
   */
   public void setUserZoom(boolean state) {
      this.mUserZoom = state;
   }

  /**
   * Set the current marker index
   * @ param index
   *
   */
   public void setMarkerIndex(int index) {
      this.mMarkerIndex = index;
   }

  /**
   * Get the current marker index
   *
   */
   public int getMarkerIndex() {
      return this.mMarkerIndex;
   }

  class GrayFilter extends RGBImageFilter {
    public GrayFilter() {canFilterIndexColorModel = true;}
    public int filterRGB(int x, int y, int rgb) {
      int a = rgb & 0xff000000;

      int r = (rgb & 0xff0000) >> 16;
      int g = (rgb & 0x00ff00) >> 8;
      int b = (rgb & 0x0000ff);
      int gray = 128 + (int)(.075 * r + .145 * g + .027 * b);
      return a | (gray << 16) | (gray << 8) | gray;
    }
  }
}
