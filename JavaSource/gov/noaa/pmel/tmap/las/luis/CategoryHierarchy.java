package gov.noaa.pmel.tmap.las.luis;

import java.sql.SQLException;
import java.util.Vector;
import java.util.ListIterator;
import gov.noaa.pmel.tmap.las.luis.*;



/**
 * @author $Author: rhs $
 * @version $Version$
 */
public class CategoryHierarchy {
 
   Vector mCategories;
   Vector mVariables;
   String mIndex;

   public CategoryHierarchy() {
      mCategories = new Vector();
      mVariables = new Vector();
      mIndex = new String();
   }

   public Vector getCategories() {
     return mCategories;
   }
 
   public void setCategories(Vector cats) {
      this.mCategories = cats;
   }
 
   public Vector getVariables() {
     return mVariables;
   }
 
   public void setVariables(Vector vars) {
      this.mVariables = vars;
   }

   public String getIndex() {
      return mIndex;
   }

   public void setIndex(String i) {
      this.mIndex = i;
   }
 
   public boolean done() {
      if ( mVariables.isEmpty() ) {
         return false;
      } else {
         return true;
      }
   }
}
