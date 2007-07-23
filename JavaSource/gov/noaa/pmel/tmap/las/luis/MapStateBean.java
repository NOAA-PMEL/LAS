package gov.noaa.pmel.tmap.las.luis;

public class MapStateBean {

   private String delayedMapOp;
   private String currentMapCommand;
   private int currentVariable;
   private String currentTool;
   private boolean needTwoVars;
   private boolean redirect;
   private int xclick;
   private int yclick;
   // These are the tool postions.
   private String xlo;
   private String xhi;
   private String ylo;
   private String yhi;
   private String xlo_compare;
   private String xhi_compare;
   private String ylo_compare;
   private String yhi_compare;
   // These are the axis dimensions of the valid region.
   private String validXlo;
   private String validXhi;
   private String validYlo;
   private String validYhi;
   private int imageID;

   public MapStateBean() {
      delayedMapOp = "none";
      currentMapCommand = "none";
      currentVariable = 1;
      currentTool = "XY";
      needTwoVars = false;
      redirect = true;
      xclick = 0;
      yclick = 0;
      xlo = "-180";
      xhi = "180";
      ylo = "-90";
      yhi = "90";
      xlo_compare = null;
      xhi_compare = null;
      ylo_compare = null;
      yhi_compare = null;
      imageID = 0;
   }

   public void setDelayedMapOp(String mapOp) {
      this.delayedMapOp = mapOp;
   }
   public String getDelayedMapOp() {
      return this.delayedMapOp;
   }

   public void setCurrentMapCommand(String command) {
      this.currentMapCommand = command;
   }
   public String getCurrentMapCommand() {
      return this.currentMapCommand;
   }

   public void setCurrentVariable(int var) {
      this.currentVariable = var;
   }
   public int getCurrentVariable() {
      return this.currentVariable;
   }

   public void setCurrentTool(String tool) {
      this.currentTool = tool;
   }
   public String getCurrentTool() {
      return this.currentTool;
   }

   public void setNeedTwoVars(boolean twovars) {
      this.needTwoVars = twovars;
   }
   public boolean getNeedTwoVars() {
      return this.needTwoVars;
   }

   public void setRedirect(boolean redirect) {
      this.redirect = redirect;
   }
   public boolean getRedirect() {
      return this.redirect;
   }

   public void setClickX(int x) {
      this.xclick = x;
   }
   public int getClickX() {
      return this.xclick;
   }

   public void setClickY(int y) {
      this.yclick = y;
   }
   public int getClickY() {
      return this.yclick;
   }

   public void setXlo(String xlo) {
      if ( this.currentVariable == 1 ) {
         this.xlo = xlo;
      }
      else {
         this.xlo_compare = xlo;
      }
   }
   public String getXlo() {
      return this.xlo;
   }
   public double getXloAsDouble() {
      return Double.valueOf(this.xlo).doubleValue();
   }

   public void setValidXlo(String xlo) {
      this.validXlo = xlo;
   }
   public String getValidXlo() {
      return this.validXlo;
   }
   public double getValidXloAsDouble() {
      return Double.valueOf(this.validXlo).doubleValue();
   }

   public void setValidXhi(String xhi) {
      this.validXhi = xhi;
   }
   public String getValidXhi() {
      return this.validXhi;
   }
   public double getValidXhiAsDouble() {
      return Double.valueOf(this.validXhi).doubleValue();
   }

   public void setValidYlo(String ylo) {
      this.validYlo = ylo;
   }
   public String getValidYlo() {
      return this.validYlo;
   }
   public double getValidYloAsDouble() {
      return Double.valueOf(this.validYlo).doubleValue();
   }

   public void setValidYhi(String yhi) {
      this.validYhi = yhi;
   }
   public String getValidYhi() {
      return this.validYhi;
   }
   public double getValidYhiAsDouble() {
      return Double.valueOf(this.validYhi).doubleValue();
   }

   public void setXhi(String xhi) {
      if ( this.currentVariable == 1 ) {
        this.xhi = xhi;
      }
      else {
        this.xhi_compare = xhi;
      }
   }
   public String getXhi() {
      if ( this.currentTool.equals("PT") ) {
         return this.xlo;
      }
      else {
         return this.xhi;
      }
   }
   public double getXhiAsDouble() {
      return Double.valueOf(this.xhi).doubleValue();
   }

   public void setYlo(String ylo) {
      if ( this.currentVariable == 1 ) {
         this.ylo = ylo;
      }
      else {
         this.ylo_compare = ylo;
      }
   }
   public String getYlo() {
      return this.ylo;
   }
   public double getYloAsDouble() {
      return Double.valueOf(this.ylo).doubleValue();
   }

   public void setYhi(String yhi) {
      if ( this.currentVariable == 1 ) {
         this.yhi = yhi;
      }
      else {
         this.yhi_compare = yhi;
      }
   }
   public String getYhi() {
      if ( this.currentTool.equals("PT") ) {
         return this.ylo;
      } 
      else {
         return this.yhi;
      }
   }
   public double getYhiAsDouble() {
      return Double.valueOf(this.yhi).doubleValue();
   }

   public void setXlo_compare(String xlo_compare) {
      this.xlo_compare = xlo_compare;
   }
   public String getXlo_compare() {
      return this.xlo_compare;
   }

   public void setXhi_compare(String xhi_compare) {
      this.xhi_compare = xhi_compare;
   }
   public String getXhi_compare() {
      if ( this.currentTool.equals("PT") ) {
         return this.xlo_compare;
      }
      else {
         return this.xhi_compare;
      }
   }

   public void setYlo_compare(String ylo_compare) {
      this.ylo_compare = ylo_compare;
   }
   public String getYlo_compare() {
      return this.ylo_compare;
   }

   public void setYhi_compare(String yhi_compare) {
      this.yhi_compare = yhi_compare;
   }
   public String getYhi_compare() {
      if ( this.currentTool.equals("PT") ) {
         return this.ylo_compare;
      }
      else {
         return this.yhi_compare;
      }
   }

   public String getXloText() {
      if (this.currentVariable == 1 ) {
         return this.xlo;
      }
      else {
         return this.xlo_compare;
      }
   }

   public String getXhiText() {
      if (this.currentVariable == 1 ) {
         return getXhi();
      }
      else {
         return getXhi_compare();
      }
   }

   public String getYloText() {
      if (this.currentVariable == 1 ) {
         return this.ylo;
      }
      else {
         return this.ylo_compare;
      }
   }

   public String getYhiText() {
      if ( this.currentVariable == 1 ) {
         return getYhi();
      }
      else {
         return getYhi_compare();
      }
   }

   public int getImageID() {
      return this.imageID;
   }
   public void setImageID(int id) {
      this.imageID = id;
   }

   public String getRandom() {
      return String.valueOf(Math.random());
   }
}




