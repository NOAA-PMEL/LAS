package gov.noaa.pmel.tmap.las.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;


public class LASProxy {
	public int streamBufferSize = 8196;
	private static Logger log = Logger.getLogger(LASProxy.class.getName());
	public void executeGetMethodAndSaveResult(String url, File outfile, HttpServletResponse response) throws IOException, HttpException {
		HttpClient client = new HttpClient();
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
					throw new IOException(method.getResponseBodyAsString());
				} else {
					response.sendError(rc);
				}
			} 
			InputStream input = method.getResponseBodyAsStream();
			OutputStream output = new FileOutputStream(outfile);
			stream(input, output);
		} finally {

			method.releaseConnection();
		}

	}
	public String executeGetMethodAndReturnResult(String url, HttpServletResponse response) throws IOException, HttpException {
		
		HttpClient client = new HttpClient();
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
		return executeGetMethodAndReturnResult(url, null);
	}
	   /**
     * Makes HTTP GET request and writes result to response output stream.
     * @param request fully qualified request URL.
     * @param response the response
     * @throws IOException
     * @throws HttpException
     */
	public InputStream executeGetMethodAndReturnStream(String request, HttpServletResponse response) throws IOException, HttpException {

	    HttpClient client = new HttpClient();
	    HttpClientParams params = client.getParams();
	    params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS,Boolean.TRUE);
	    client.setParams(params);
	    GetMethod method = new GetMethod(request);

	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
	    method.setRequestHeader("Connection", "close");

	    log.info("method: " + method.getURI());

	    int rc = client.executeMethod(method);

	    if (rc != HttpStatus.SC_OK) {

	        log.error("HttpGet Error Code: "+rc);

	        //response.sendError(rc);

	        return null;
	    }
	    return method.getResponseBodyAsStream();

	}
	/**
	 * Makes HTTP GET request and writes result to response output stream.
	 * @param request fully qualified request URL.
	 * @param response the response
	 * @throws IOException
	 * @throws HttpException
	 */
	public void executeGetMethodAndStreamResult(String request, HttpServletResponse response) throws IOException, HttpException {
		HttpClient client = new HttpClient();
		HttpClientParams params = client.getParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS,Boolean.TRUE);
		client.setParams(params);
		GetMethod method = new GetMethod(request);

		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		method.setRequestHeader("Connection", "close");

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
		method.setRequestHeader("Connection", "close");

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
