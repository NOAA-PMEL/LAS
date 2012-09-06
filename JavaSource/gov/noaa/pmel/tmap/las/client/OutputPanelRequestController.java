/**
 * 
 */
package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.event.LASRequestEvent;
import gov.noaa.pmel.tmap.las.client.laswidget.OutputPanel;
import gov.noaa.pmel.tmap.las.client.util.MultiCallback;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashMap;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;

/**
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
	}

	/**
	 * @see gov.noaa.pmel.tmap.las.client.RequestController#done(gov.noaa.pmel.tmap.las.client.util.MultiCallback)
	 */
	@Override
	public void done(MultiCallback multiCallback) {
		seenUrls.remove(multiCallback.getUrl());
	}

	/**
	 * @see gov.noaa.pmel.tmap.las.client.RequestController#process(gov.noaa.pmel.tmap.las.client.event.LASRequestEvent)
	 */
	@Override
	public void process(LASRequestEvent event) {
		Object source = event.getSource();
		OutputPanel sourceOutputPanel = (OutputPanel) source;
		String sourceName = sourceOutputPanel.getName();
		logger.info("sourceOutputPanel.getName():" + sourceName);
		String url = event.getUrl();
		String requestCallbackObjectName = event.getRequestCallbackObjectName();
		if (seenUrls.containsKey(url)) {
			logger.info("Already seen url:" + url);
			// Set the multiCallback for the current url to use
			// the current requestCallback when the request is done
			MultiCallback multiCallback = seenUrls.get(url);
			multiCallback.add(sourceName, requestCallbackObjectName);
			// save the current url and multiCallback back into
			// seenUrls
			seenUrls.put(url, multiCallback);
			logger.info("Just added sourceName:" + sourceName
					+ " to multiCallback:" + multiCallback);
		} else {
			try {
				RequestBuilder sendRequest = new RequestBuilder(
						event.getMethod(), url);
				// Set a new multiCallback to use the current
				// requestCallback when the request is done
				MultiCallback multiCallback = new MultiCallback(this, url,
						sourceName, requestCallbackObjectName);
				// save the current url and multiCallback in seenUrls,
				seenUrls.put(url, multiCallback);
				// then send the request with multiCallback
				Request request = sendRequest.sendRequest(null, multiCallback);
				logger.info("Just called sendRequest for sourceName:" + sourceName
					+ " with multiCallback:" + multiCallback);
				logger.info("request:" + request);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

}
