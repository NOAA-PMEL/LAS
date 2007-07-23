package gov.noaa.pmel.tmap.las.luis;
import javax.servlet.ServletException;
import java.sql.SQLException;
import org.apache.velocity.*;
import gov.noaa.pmel.tmap.las.luis.db.*;
import java.util.Vector;

public class MetadataBean extends DefaultTemplateBean {

  public void init(TemplateContext tc) throws ServletException, SQLException {
    super.init(tc);
    String catNum = mReq.getParameter("catitem");
    if (catNum == null){
      return;
    }
    DatasetItem di = DatasetItem.getInstance(catNum);
    tc.put("metadata_dataset", di);
    Vector derived = DatasetItem.getDerivedItems(getSession(), catNum);
    tc.put("metadata_dataset_derived", derived);
    MetaData md = di.getMetaData();
    tc.put("metadata", md);
    Vector contributors = di.getContributors();
    tc.put("contributors", contributors);
  }
}
