package gov.noaa.pmel.tmap.las.luis;
import java.lang.String;
import javax.servlet.ServletException;
import java.sql.SQLException;

public interface TemplateBean {
  public void init(TemplateContext tc) throws ServletException, SQLException;
  public String getTemplateName() throws ServletException, SQLException;
  public boolean useTemplate();
}
