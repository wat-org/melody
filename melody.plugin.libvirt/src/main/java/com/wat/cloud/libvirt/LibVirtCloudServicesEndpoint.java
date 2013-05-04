package com.wat.cloud.libvirt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;

/**
 * <p>
 * Enable the LibVirtCloud Rest WebServices endpoint.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtCloudServicesEndpoint {

	private static Log log = LogFactory
			.getLog(LibVirtCloudServicesEndpoint.class);

	/*
	 * TODO : extarnalize listen ip and listen port
	 */
	public static final String SERVER_ENDPOINT = "http://0.0.0.0:6060/LibVirtCloudServices";

	private static LibVirtCloudServicesEndpoint _singleton = null;

	public static void start() {
		if (_singleton != null) {
			return;
		}
		_singleton = new LibVirtCloudServicesEndpoint();
	}

	private JAXRSServerFactoryBean _restServer;

	private LibVirtCloudServicesEndpoint() {
		try {
			log.info("Starting server on : [" + SERVER_ENDPOINT + "]");
			LibVirtCloudRestServices lvService = new LibVirtCloudRestServicesImpl();
			// REST based service Server Setting.
			_restServer = new JAXRSServerFactoryBean();
			_restServer.setServiceBean(lvService);
			_restServer.setAddress(SERVER_ENDPOINT);
			_restServer.create();
		} catch (Throwable t) {
			throw new RuntimeException("Server encounter error ["
					+ SERVER_ENDPOINT + "]", t);
		}
	}

}