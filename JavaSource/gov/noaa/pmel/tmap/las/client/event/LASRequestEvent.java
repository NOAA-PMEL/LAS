/**
 * 
 */
package gov.noaa.pmel.tmap.las.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.RequestBuilder;

/**
 * A {@link GwtEvent} that informs listeners of a LAS Request URL url change.
 * Listeners must have a class that implements (or implement themselves) the
 * {@link LASRequestEvent.Handler} interface.
 * 
 * @author weusijana
 * 
 */
public class LASRequestEvent extends GwtEvent<LASRequestEvent.Handler> {

    public interface Handler extends EventHandler {
        void onRequest(LASRequestEvent event);
    }

    public static final Type<LASRequestEvent.Handler> TYPE = new Type<LASRequestEvent.Handler>();

    private String url;
    private RequestBuilder.Method method = RequestBuilder.GET;
    private String requestCallbackObjectName;

    public LASRequestEvent(String url, String requestCallbackObjectName) {
        this.url = url;
        this.requestCallbackObjectName = requestCallbackObjectName;
    }

    public LASRequestEvent(String url, RequestBuilder.Method method, String requestCallbackObjectName) {
        this.url = url;
        this.method = method;
        this.requestCallbackObjectName = requestCallbackObjectName;
    }

    @Override
    protected void dispatch(LASRequestEvent.Handler handler) {
        handler.onRequest(this);
    }

    @Override
    public final Type<LASRequestEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    void setUrl(String url) {
        this.url = url;
    }

	/**
	 * @return the requestCallbackObjectName
	 */
	public String getRequestCallbackObjectName() {
		return requestCallbackObjectName;
	}

	/**
	 * @param requestCallbackObjectName the requestCallbackObjectName to set
	 */
	void setRequestCallbackObjectName(String requestCallbackObjectName) {
		this.requestCallbackObjectName = requestCallbackObjectName;
	}

	/**
	 * @return the method
	 */
	public RequestBuilder.Method getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	void setMethod(RequestBuilder.Method method) {
		this.method = method;
	}
}
