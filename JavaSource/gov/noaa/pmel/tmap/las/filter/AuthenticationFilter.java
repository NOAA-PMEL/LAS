package gov.noaa.pmel.tmap.las.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import oracle.jdbc.rowset.OracleWebRowSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.security.Principal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


public class AuthenticationFilter implements Filter {
	private static Logger log = LogManager.getLogger(AuthenticationFilter.class.getName());
	protected FilterConfig filterConfig;

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) {
		log.debug("Authentication: Request received");
		HttpServletRequest  request = (HttpServletRequest)  servletRequest;
		HttpServletResponse  response = (HttpServletResponse) servletResponse;

		Principal principal = request.getUserPrincipal();
		HttpSession session = request.getSession(false);

		if (session==null || principal == null ) {
			// if custom authentication is used, do something here. If tomcat authentication is used, nothing needs to be done here.
		}
		else {
			// stick the user_name in the session as well so that we don't have to look it up in the database every single time a page is refreshed/updated.
			String user_name = session.getAttribute("user_name") == null ? null : session.getAttribute("user_name").toString();
			if(user_name == null || user_name.equals("")){
				try{
					String lookup_name = filterConfig.getInitParameter("lookup_name");
					if(lookup_name!=null && lookup_name.toLowerCase().equals("true")){
						user_name = lookup(request, principal.getName());
					}
					else{
						user_name=principal.getName();
					}
					request.setAttribute("user_name", user_name);
					session.setAttribute("user_name", user_name);
					log.info("user " + user_name + " logging in");
				}
				catch(Exception e){
					//request.setAttribute("user_name", "");
					log.error("Error logging user in: username " + principal.getName() + "; error: " + e.getMessage());
				}
			}
		}

		try{
			// procede if user is authenticated
			chain.doFilter(request, response);
		}
		catch(Exception e){
			log.error("Error in AuthenticationFilter - " + e.getMessage());
			// now what?
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
	}

	public void destroy() { }

	private String lookup(HttpServletRequest request, String name){
		String user_name = name;
		try{
			String db_type = filterConfig.getInitParameter("db_type");

			if(db_type != null){
				String db_login = filterConfig.getInitParameter("db_login");
				String db_passwd = filterConfig.getInitParameter("db_passwd");
				String db_host = filterConfig.getInitParameter("db_host");
				String db_port = filterConfig.getInitParameter("db_port");
				String db_name = filterConfig.getInitParameter("db_name");
				String auth_user_display_name_field = filterConfig.getInitParameter("auth_user_display_name_field");
				String auth_user_name_field = filterConfig.getInitParameter("auth_user_name_field");
				String auth_table_name = filterConfig.getInitParameter("auth_table_name");

				Connection con = null;

				Properties connectionProps = new Properties();
				connectionProps.put("user", db_login);
				connectionProps.put("password", db_passwd);

				if (db_type.equals("mysql")) {
					Class.forName("com.mysql.jdbc.Driver");
					String connectString = "jdbc:" + db_type + "://" + db_host;
					if(db_port != null && !db_port.equals(""))
						connectString += ":" + db_port;
					connectString += "/";
					con = DriverManager.getConnection(connectString, connectionProps);
				}
				// else if(db_type.equals("oracle"){...}

				Statement stmt = null;
				String query2 =
					"select " + auth_user_display_name_field + " from " + db_name + "." + auth_table_name + " where " + auth_user_name_field + "='" + name + "'";
				try {
					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(query2);
					while (rs.next()) {
						name = rs.getString("reviewer");
						break;
					}
				}
				catch (SQLException e ) {
					log.error("Error looking up user information - " + e.getMessage());
				} finally {
					if (stmt != null) { stmt.close(); }
					if (con!=null) { con.close();}
				}
			}
			else{
				// add other lookups (flat file, etc.) here as desired
			}
		}
		catch(Exception e){
			log.error("Error looking up user information - " + e.getMessage());
		}
		return name;
	}
}