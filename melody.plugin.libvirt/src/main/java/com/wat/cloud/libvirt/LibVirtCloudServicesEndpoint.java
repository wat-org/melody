package com.wat.cloud.libvirt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;

import com.wat.melody.common.endpoint.ContextRoot;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

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

	private static LibVirtCloudServicesEndpoint _singleton = null;

	public static LibVirtCloudServicesEndpoint getInstance() {
		if (_singleton == null) {
			_singleton = new LibVirtCloudServicesEndpoint();
		}
		return _singleton;
	}

	private boolean _endpointSecure = false;
	private Host _endpointListenIp = null;
	private Port _endpointListenPort = null;
	private ContextRoot _endpointContextRoot = null;
	private JAXRSServerFactoryBean _restServer;

	public void start(boolean secure, Host host, Port port, ContextRoot ctxRoot) {
		setEndpointSecure(secure);
		setEndpointListenIp(host);
		setEndpointListenPort(port);
		setEndpointContextRoot(ctxRoot);
		try {
			log.info("Starting server [" + getEndpoint() + "].");
			// REST based service Server Setting.
			_restServer = new JAXRSServerFactoryBean();
			_restServer.setServiceBean(new LibVirtCloudRestServicesImpl());
			_restServer.setAddress(getEndpoint());
			_restServer.create();
		} catch (Throwable Ex) {
			throw new RuntimeException("Fail to start server [" + getEndpoint()
					+ "].", Ex);
		}
	}

	public String getEndpoint() {
		return (getEndpointSecure() ? "https" : "http") + "//"
				+ getEndpointListenIp().getAddress() + ":"
				+ getEndpointListenPort() + "/" + getEndpointContextRoot();
	}

	public boolean getEndpointSecure() {
		return _endpointSecure;
	}

	public boolean setEndpointSecure(boolean secure) {
		boolean previous = getEndpointSecure();
		_endpointSecure = secure;
		return previous;
	}

	public Host getEndpointListenIp() {
		return _endpointListenIp;
	}

	public Host setEndpointListenIp(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getEndpointListenIp();
		_endpointListenIp = host;
		return previous;
	}

	public Port getEndpointListenPort() {
		return _endpointListenPort;
	}

	public Port setEndpointListenPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getEndpointListenPort();
		_endpointListenPort = port;
		return previous;
	}

	public ContextRoot getEndpointContextRoot() {
		return _endpointContextRoot;
	}

	public ContextRoot setEndpointContextRoot(ContextRoot contextRoot) {
		if (contextRoot == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ContextRoot.class.getCanonicalName()
					+ ".");
		}
		ContextRoot previous = getEndpointContextRoot();
		_endpointContextRoot = contextRoot;
		return previous;
	}

}