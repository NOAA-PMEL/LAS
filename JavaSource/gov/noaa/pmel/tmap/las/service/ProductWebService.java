/**
 * 
 */
package gov.noaa.pmel.tmap.las.service;

import gov.noaa.pmel.tmap.exception.LASException;
import gov.noaa.pmel.tmap.jdom.LASDocument;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class ProductWebService extends ProductService {
    
    public ProductWebService (LASBackendRequest lasBackendRequest, String serverURL, String methodName, String outputFileName) throws LASException, IOException {
        super(lasBackendRequest, serverURL, methodName, outputFileName);
    }
    


    // TODO this could just throw an exception for each of these, yes?
    public void run() {
            
            if ( lasBackendRequest.toString().equals("") || lasBackendRequest == null ) {
                responseXML = error_response("Backend request XML is blank or null.  Check product server config file.");
                return;
            }
            if ( serverURL.equals("") || serverURL == null ) {
                responseXML = error_response("Backend server URL blank or null.  Check product server config file.");
                return;
            }
            if ( methodName.equals("") || methodName == null ) {
                responseXML = error_response("Service name is blank or null.  Check product server config file.");
                return;
            }
            
            Service  service = new Service();
            Call call=null;
            try {
                call = (Call) service.createCall();
            } catch (ServiceException e) {
                // TODO Auto-generated catch block
                responseXML = error_response(e.toString());
            }

            try {
                call.setTargetEndpointAddress( new java.net.URL(serverURL) );
            } catch (MalformedURLException e) {
                responseXML = error_response(e.toString());
            }
            call.setOperationName( new QName("LASBackend", methodName) );
            call.addParameter( "lasBackendRequest", org.apache.axis.encoding.XMLType.XSD_STRING, ParameterMode.IN );
            call.addParameter( "outputFileName", org.apache.axis.encoding.XMLType.XSD_STRING, ParameterMode.IN );
            call.setReturnType( org.apache.axis.encoding.XMLType.XSD_STRING );
            // TODO set this time out value to be larger than the backend product time out...
            
            long t = lasBackendRequest.getProductTimeout();
            if ( t > timeout ) {
            	call.setTimeout((int)t*1000 + (int)t/10);
            } else {
               call.setTimeout(timeout*1000);
            }
            try {
                responseXML = (String) call.invoke( new Object[] {lasBackendRequest.toString(), outputFileName} );
            } catch (RemoteException e) {
                responseXML = error_response("The backend service threw an uncaught exception", e);
            }
    }

 
}
