package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.product.server.ProductServerAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LASProxy {
	private int streamBufferSize = 8196;
	private static Logger log = LogManager.getLogger(LASProxy.class.getName());
	public String executeGetMethodAndReturnResult(String url, HttpServletResponse response, String id) throws IOException, HttpException {
		HttpState initialState = new HttpState();
		URL urlo = new URL(url);
		Cookie cookie = new Cookie(urlo.getHost(), "esg.openid.identity.cookie", id);
		initialState.addCookie(cookie);
		HttpClient client = new HttpClient();
		client.setState(initialState);
		HttpClientParams params = client.getParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS,Boolean.TRUE);
		client.setParams(params);
        GetMethod method = new GetMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {

			log.info("method: " + method.getURI());

			int rc = client.executeMethod(method);

			if (rc != HttpStatus.SC_OK) {

				log.error("HttpGet Error Code: "+rc);
                if ( response == null ) {
                	throw new IOException("Unable to execute method.  RC="+rc);
                } else {
				    response.sendError(rc);
                }

				return null;

			}
			return method.getResponseBodyAsString();
		}
		finally {

			method.releaseConnection();
		}
	}
	public String executeGetMethodAndReturnResult(String url) throws HttpException, IOException {
		return executeGetMethodAndReturnResult(url, null, null);
	}
	public String executeGetMethodAndReturnResult(String url, String id) throws HttpException, IOException {
		return executeGetMethodAndReturnResult(url, null, id);
	}
	/**
	 * Makes HTTP GET request and writes result to response output stream.
	 * @param request fully qualified request URL.
	 * @param response the response
	 * @throws IOException
	 * @throws HttpException
	 */
	public void executeGetMethodAndStreamResult(String request, HttpServletResponse response, String openid) throws IOException, HttpException {

		HttpState state = null;
		
		if ( openid != null ) {
			state = new HttpState();
			URL url = new URL(request);
			Cookie cookie = new Cookie(url.getHost(), "esg.openid.identity.cookie", openid);
			state.addCookie(cookie);
		}
		HttpClient client = new HttpClient();
		if ( state != null ) {
			client.setState(state);
		}
		HttpClientParams params = client.getParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS,Boolean.TRUE);
		client.setParams(params);
		GetMethod method = new GetMethod(request);

		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {

			log.info("method: " + method.getURI());

			int rc = client.executeMethod(method);

			if (rc != HttpStatus.SC_OK) {

				log.error("HttpGet Error Code: "+rc);

				response.sendError(rc);

				return;
			}

			streamGetMethodResponse(method,response.getOutputStream());
		}
		finally {

			method.releaseConnection();
		}
	}
	/**
	 * Makes HTTP GET request and writes result to response output stream.
	 * @param request fully qualified request URL.
	 * @param output OutputStream to write to
	 * @throws IOException
	 * @throws HttpException
	 */
	public void executeGetMethodAndStreamResult(String request, OutputStream output) throws IOException, HttpException {

		HttpClient client = new HttpClient();

		GetMethod method = new GetMethod(request);
		HttpClientParams params = client.getParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS,Boolean.TRUE);
		client.setParams(params);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {

			log.info("method: " + method.getURI());

			int rc = client.executeMethod(method);

			if (rc != HttpStatus.SC_OK) {

				log.error("HttpGet Error Code: "+rc);

				HttpException exception = new HttpException("HTTP Get returned error: " + rc);

				throw exception;
			}

			streamGetMethodResponse(method,output);
		}
		finally {

			method.releaseConnection();
		}

		log.info("done: " + method.getURI());
	}

	public void streamGetMethodResponse(GetMethod method, OutputStream output) throws IOException, HttpException {

		InputStream input = method.getResponseBodyAsStream();
		stream(input, output);

	}

	public void stream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[streamBufferSize];
		int count = input.read(buffer);

		while( count != -1 && count <= streamBufferSize ) {

			output.write(buffer,0,count);
			count = input.read(buffer);
		}

		input.close();
		output.close();
	}
}
