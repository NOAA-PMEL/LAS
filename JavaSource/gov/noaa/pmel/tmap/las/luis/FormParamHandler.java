package gov.noaa.pmel.tmap.las.luis;

import java.util.Vector;
import java.lang.RuntimeException;


public class FormParamHandler {
  static public interface Variable {
    public FormParameters getVariables();
    public void setVariables(FormParameters params);
    public Vector getDatasets();
    public void setDatasets(Vector v);
    public void setRegion(RegionConstraint rc);
    public RegionConstraint getRegion();
  }

  static public class AbstractVariable implements Variable {
    public FormParameters getVariables(){
      throw new RuntimeException("Not implemented");
    }
    
    public void setVariables(FormParameters params){
      throw new RuntimeException("Not implemented");
    }
    
    public Vector getDatasets(){
      throw new RuntimeException("Not implemented");
    }
    
    public void setDatasets(Vector v){
      throw new RuntimeException("Not implemented");
    }
    
    public void setRegion(RegionConstraint rc){
      throw new RuntimeException("Not implemented");
    }
    
    public RegionConstraint getRegion(){
      throw new RuntimeException("Not implemented");
    }
    
  }

  static public class SingleVariable extends AbstractVariable{
    TemplateSession mSession;
    public SingleVariable(TemplateSession session){
      mSession = session;
    }
    public FormParameters getVariables() {
      if (mSession == null){
	return null;
      }
      return mSession.getSessionObject().getVariables();
    }
    public void setVariables(FormParameters params){
      if (mSession == null){
	return;
      }
      mSession.getSessionObject().setVariables(params);
    }
    public Vector getDatasets() {
      if (mSession == null){
	return null;
      }
      return mSession.getSessionContext().getDatasets();
    }
    public void setDatasets(Vector v) {
      if (mSession == null){
	return;
      }
      mSession.getSessionContext().setDatasets(v);
    }
    public void setRegion(RegionConstraint rc){
      if (mSession == null){
	return;
      }
      mSession.getSessionContext().setRegion(rc);
    }
    
    public RegionConstraint getRegion(){
      if (mSession == null){
	return null;
      }
      return mSession.getSessionContext().getRegion();
    }
    
  }

  static public class CompareVariable extends AbstractVariable{
    TemplateSession mSession;
    int mIndex;
    public CompareVariable(int index, TemplateSession session){
      mSession = session;
      mIndex = index;
    }
    public FormParameters getVariables() {
      if (mSession == null){
	return null;
      }
      return mSession.getSessionObject().getCompareVariables(mIndex);
    }
    public void setVariables(FormParameters params){
      if (mSession == null){
	return;
      }
      mSession.getSessionObject().setCompareVariables(mIndex, params);
    }
    public Vector getDatasets() {
      if (mSession == null){
	return null;
      }
      Object[] rval = mSession.getSessionContext().getCompareDatasets().toArray();
      return (Vector)rval[mIndex];
    }
    public void setDatasets(Vector v) {
      if (mSession == null){
	return;
      }
      mSession.getSessionContext().setCompareDatasets(mIndex, v);
    }
    public void setRegion(RegionConstraint rc){
      if (mSession == null){
	return;
      }
      mSession.getSessionContext().setCompareRegion(mIndex, rc);
    }
    
    public RegionConstraint getRegion(){
      if (mSession == null){
	return null;
      }
      return mSession.getSessionContext().getCompareRegion(mIndex);
    }
  }
}
