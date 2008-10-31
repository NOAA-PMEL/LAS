package gov.noaa.pmel.tmap.addxml;

import gov.noaa.pmel.tmap.addxml.LASCredentialsProvider;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;

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
	
	public final Credentials getCredentials(AuthScheme arg0, String arg1, int arg2, boolean arg3) throws CredentialsNotAvailableException{
		// Since data is accessed through a single HTTP session, the server need only
		// authenticate once. However, the server may be configured to repeat requesting 
		// the provider in case authentication failed. Here we prevent the infinite loop
		// by generating an exception on multiple requests.
		if(!first){
			throw new CredentialsNotAvailableException();
		}
		first = false;
		return new UsernamePasswordCredentials(username, password);
	}
}

