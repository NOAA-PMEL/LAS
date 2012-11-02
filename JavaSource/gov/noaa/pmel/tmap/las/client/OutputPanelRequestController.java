/**
 * 
 */
package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.event.LASRequestEvent;
import gov.noaa.pmel.tmap.las.client.event.LASResponseEvent;
import gov.noaa.pmel.tmap.las.client.laswidget.OutputPanel;
import gov.noaa.pmel.tmap.las.client.util.MultiCallback;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

/**
 * This controller was written to manage LAS requests from {@link OutputPanel}s
 * to address Trac ticket #1351: Make the client smart enough to only submit one
 * copy of a particular request.<br/>
 * 
 * When the client was doing a 4-panel plot of the same thing the logic in the
 * server was not quite 'smart' enough to prevent the same request from making
 * it to the Ferret backend service. Because of this the ProductServer ended up
 * writing to the same map_scale file with two different threads.<br/>
 * 
 * Thus the client was made 'smarter' to prevent it from asking for the same
 * thing twice. This was done by having the {@link OutputPanel}s send their
 * independent requests to an object of this class that will collect the
 * requests and only send one copy of each and notify all the
 * {@link OutputPanel}s with the results when they finish.<br/>
 * 
 * Inter-object communication is implemented using the GWT event bus.
 * {@link OutputPanel}s send {@link LASRequestEvent}s that request a particular
 * product, the {@link OutputPanelRequestController} listens for those events
 * and either submits the URL to the product server if it is new (and assigns it
 * a {@link MultiCallback}) or ignores the request if that URL has already been
 * submitted (and adds another callback to the appropriate {@link MultiCallback}
 * ).<br/>
 * 
 * Once the request returns, the appropriate {@link MultiCallback} sends
 * {@link LASResponseEvent}s saying the product has returned and includes the
 * {@link Response} and the original {@link Request} in the event. The
 * {@link OutputPanel}s are listening for those product return
 * {@link LASResponseEvent}s and update accordingly.
 * 
 * @author weusijana
 * 
 */
public class OutputPanelRequestController implements RequestController {
	private final Logger logger = Logger
			.getLogger(OutputPanelRequestController.class.getName());
	private HashMap<String, MultiCallback> seenUrls = new HashMap<String, MultiCallback>();

	/**
	 * 
	 */
	public OutputPanelRequestController() {
		super();
		logger.setLevel(Level.OFF);
	}

	/**
	 * @see gov.noaa.pmel.tmap.las.client.RequestController#done(gov.noaa.pmel.tmap.las.client.util.MultiCallback)
	 */
	@Override
	public void done(MultiCallback multiCallback) {
		String url = multiCallback.getUrl();
		Object previousValue = seenUrls.remove(url);
		logger.info("done with seen multiCallback url:" + url + " and value:"
				+ previousValue.toString());
	}

	/**
	 * @see gov.noaa.pmel.tmap.las.client.RequestController#process(gov.noaa.pmel.tmap.las.client.event.LASRequestEvent)
	 */
	@Override
	public void process(LASRequestEvent event) {
		Object source = event.getSource();
		String sourceString = source.toString();
		if ((source instanceof OutputPanel)
				|| (sourceString.contains(".OutputPanel$"))) {
			String sourceName = event.getSourceName();
			logger.info("event.getSourceName():" + sourceName);
			String url = event.getUrl();
			String requestCallbackObjectName = event
					.getRequestCallbackObjectName();
			if (seenUrls.containsKey(url)) {
				logger.info("Already seen url:" + url);
				// Set the multiCallback for the current url to use
				// the current requestCallback when the request is done
				MultiCallback multiCallback = seenUrls.get(url);
				boolean addSucessful = multiCallback.add(sourceName,
						requestCallbackObjectName);
				logger.info("Attempt to add sourceName:" + sourceName
						+ ", requestCallbackObjectName:"
						+ requestCallbackObjectName + " to multiCallback:"
						+ multiCallback + " was:" + addSucessful);
				// save the current url and multiCallback back into
				// seenUrls
				Object previousValue = seenUrls.put(url, multiCallback);
				if (previousValue == null)
					previousValue = "null";
				logger.info("seenUrls.put(url, multiCallback) returned previousValue:"+previousValue);
			} else {
				try {
					RequestBuilder sendRequest = event.getRequestBuilder();
					// Set a new multiCallback to use the current
					// requestCallback when the request is done
					MultiCallback multiCallback = new MultiCallback(this, url,
							sourceName, requestCallbackObjectName);
					// save the current url and multiCallback in seenUrls,
					seenUrls.put(url, multiCallback);
					// then send the request with multiCallback
					logger.info("Calling sendRequest for sourceName:"
							+ sourceName + " with multiCallback:"
							+ multiCallback);
					Request request = sendRequest.sendRequest(null,
							multiCallback);
					logger.info("request:" + request);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		} else {
			logger.warning("The source is NOT and instanceof OutputPanel. source:"
					+ sourceString);
		}
	}

}
