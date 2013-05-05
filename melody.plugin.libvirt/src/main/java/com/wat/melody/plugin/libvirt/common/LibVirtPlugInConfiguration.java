package com.wat.melody.plugin.libvirt.common;

import com.wat.cloud.libvirt.LibVirtCloudServicesEndpoint;
import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.endpoint.ContextRoot;
import com.wat.melody.common.endpoint.exception.IllegalContextRootException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtPlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtPlugInConfiguration implements IPlugInConfiguration {

	public static LibVirtPlugInConfiguration get(IProcessorManager pm)
			throws PlugInConfigurationException {
		return (LibVirtPlugInConfiguration) pm
				.getPluginConfiguration(LibVirtPlugInConfiguration.class);
	}

	private static Host createHost(String host) {
		try {
			return Host.parseString(host);
		} catch (IllegalHostException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Host with value '" + host + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static Port createPort(String port) {
		try {
			return Port.parseString(port);
		} catch (IllegalPortException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Port with value '" + port + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ContextRoot createContextRoot(String contextRoot) {
		try {
			return ContextRoot.parseString(contextRoot);
		} catch (IllegalContextRootException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ContextRoot with value '" + contextRoot + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	// CONFIGURATION DIRECTIVES DEFAULT VALUES
	public final boolean DEFAULT_ENDPOINT_SECURE = false;
	public final Host DEFAULT_ENDPOINT_LISTEN_IP = createHost("0.0.0.0");
	public final Port DEFAULT_ENDPOINT_LISTEN_PORT = createPort("6060");
	public final ContextRoot DEFAULT_ENDPOINT_CONTEXT_ROOT = createContextRoot("LibVirtCloudServices");

	// MANDATORY CONFIGURATION DIRECTIVE
	public final String ENDPOINT_SECURE = "endpoint.secure";
	public final String ENDPOINT_LISTEN_IP = "endpoint.listen.ip";
	public final String ENDPOINT_LISTEN_PORT = "endpoint.listen.port";
	public final String ENDPOINT_CONTEXT_ROOT = "endpoint.contextroot";

	// OPTIONNAL CONFIGURATION DIRECTIVE

	private String msConfigurationFilePath;
	private boolean _endpointSecure = DEFAULT_ENDPOINT_SECURE;
	private Host _endpointListenIp = DEFAULT_ENDPOINT_LISTEN_IP;
	private Port _endpointListenPort = DEFAULT_ENDPOINT_LISTEN_PORT;
	private ContextRoot _endpointContextRoot = DEFAULT_ENDPOINT_CONTEXT_ROOT;

	public LibVirtPlugInConfiguration() {
	}

	@Override
	public String getFilePath() {
		return msConfigurationFilePath;
	}

	private void setFilePath(String fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a LibVirt Plug-In "
					+ "Configuration file path).");
		}
		msConfigurationFilePath = fp;
	}

	@Override
	public void load(PropertiesSet ps)
			throws LibVirtPlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ PropertiesSet.class.getCanonicalName() + ".");
		}
		setFilePath(ps.getFilePath());

		// load and validate each configuration directives
		loadEndpointSecure(ps);
		loadEndpointListenIp(ps);
		loadEndpointListenPort(ps);
		loadEndpointContextRoot(ps);

		validate();
	}

	private void loadEndpointSecure(PropertiesSet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_SECURE)) {
			return;
		}
		try {
			setEndpointSecure(ps.get(ENDPOINT_SECURE));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_SECURE), Ex);
		}
	}

	private void loadEndpointListenIp(PropertiesSet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_LISTEN_IP)) {
			return;
		}
		try {
			setEndpointListenIp(ps.get(ENDPOINT_LISTEN_IP));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_LISTEN_IP), Ex);
		}
	}

	private void loadEndpointListenPort(PropertiesSet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_LISTEN_PORT)) {
			return;
		}
		try {
			setEndpointListenPort(ps.get(ENDPOINT_LISTEN_PORT));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_LISTEN_PORT),
					Ex);
		}
	}

	private void loadEndpointContextRoot(PropertiesSet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_CONTEXT_ROOT)) {
			return;
		}
		try {
			setEndpointContextRoot(ps.get(ENDPOINT_CONTEXT_ROOT));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_CONTEXT_ROOT),
					Ex);
		}
	}

	private void validate() throws LibVirtPlugInConfigurationException {
		// validate all configuration directives
		// almost nothing to

		// Start the LibVirtCloudServicesEndpoint
		LibVirtCloudServicesEndpoint.getInstance().start(getEndpointSecure(),
				getEndpointListenIp(), getEndpointListenPort(),
				getEndpointContextRoot());
	}

	public boolean getEndpointSecure() {
		return _endpointSecure;
	}

	public boolean setEndpointSecure(boolean secure) {
		boolean previous = getEndpointSecure();
		_endpointSecure = secure;
		return previous;
	}

	public boolean setEndpointSecure(String secure)
			throws LibVirtPlugInConfigurationException {
		if (secure == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (secure.trim().length() == 0) {
			throw new LibVirtPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setEndpointSecure(Boolean.parseBoolean(secure));
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

	public Host setEndpointListenIp(String ip)
			throws LibVirtPlugInConfigurationException {
		try {
			return setEndpointListenIp(Host.parseString(ip));
		} catch (IllegalHostException Ex) {
			throw new LibVirtPlugInConfigurationException(Ex);
		}
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

	public Port setEndpointListenPort(String port)
			throws LibVirtPlugInConfigurationException {
		try {
			return setEndpointListenPort(Port.parseString(port));
		} catch (IllegalPortException Ex) {
			throw new LibVirtPlugInConfigurationException(Ex);
		}
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

	public ContextRoot setEndpointContextRoot(String contextRoot)
			throws LibVirtPlugInConfigurationException {
		try {
			return setEndpointContextRoot(ContextRoot.parseString(contextRoot));
		} catch (IllegalContextRootException Ex) {
			throw new LibVirtPlugInConfigurationException(Ex);
		}
	}

}