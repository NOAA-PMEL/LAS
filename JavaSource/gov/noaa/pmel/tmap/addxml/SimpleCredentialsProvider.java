package gov.noaa.pmel.tmap.addxml;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.mysql.jdbc.ConnectionFeatureNotAvailableException;

import gov.noaa.pmel.tmap.addxml.LASCredentialsProvider;


public class SimpleCredentialsProvider extends LASCredentialsProvider{

	private boolean first = true;    // Flag to monitor if this is the first request for credentials. 
	private String username = null;   
	private String password = null;
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public final Credentials getCredentials(AuthScheme arg0, String arg1, int arg2, boolean arg3) throws ConnectionFeatureNotAvailableException{
		// Since data is accessed through a single HTTP session, the server need only
		// authenticate once. However, the server may be configured to repeat requesting 
		// the provider in case authentication failed. Here we prevent the infinite loop
		// by generating an exception on multiple requests.
		if(!first){
			throw new ConnectionFeatureNotAvailableException(null, 4l, null);
		}
		first = false;
		return new UsernamePasswordCredentials(username, password);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Credentials getCredentials(AuthScope arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCredentials(AuthScope arg0, Credentials arg1) {
		// TODO Auto-generated method stub
		
	}
}

