package com.wat.melody.plugin.libvirt.common;

import org.libvirt.Connect;

import com.wat.cloud.libvirt.LibVirtCloudServicesEndpoint;
import com.wat.cloud.libvirt.LibVirtPooledConnection;
import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.endpoint.ContextRoot;
import com.wat.melody.common.endpoint.exception.IllegalContextRootException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.properties.PropertySet;
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

	private static Port createPort(int port) {
		try {
			return Port.parseInt(port);
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
	public static final boolean DEFAULT_ENDPOINT_ENABLED = true;
	public static final boolean DEFAULT_ENDPOINT_SECURED = false;
	public static final Host DEFAULT_ENDPOINT_LISTEN_IP = createHost("0.0.0.0");
	public static final Port DEFAULT_ENDPOINT_LISTEN_PORT = createPort(6060);
	public static final ContextRoot DEFAULT_ENDPOINT_CONTEXT_ROOT = createContextRoot("LibVirtCloudServices");

	// OPTIONNAL CONFIGURATION DIRECTIVE
	public static final String ENDPOINT_ENABLED = "endpoint.enabled";
	public static final String ENDPOINT_SECURED = "endpoint.secured";
	public static final String ENDPOINT_LISTEN_IP = "endpoint.listen.ip";
	public static final String ENDPOINT_LISTEN_PORT = "endpoint.listen.port";
	public static final String ENDPOINT_CONTEXT_ROOT = "endpoint.contextroot";

	private String _configurationFilePath;
	private boolean _endpointEnabled = DEFAULT_ENDPOINT_ENABLED;
	private boolean _endpointSecured = DEFAULT_ENDPOINT_SECURED;
	private Host _endpointListenIp = DEFAULT_ENDPOINT_LISTEN_IP;
	private Port _endpointListenPort = DEFAULT_ENDPOINT_LISTEN_PORT;
	private ContextRoot _endpointContextRoot = DEFAULT_ENDPOINT_CONTEXT_ROOT;

	public LibVirtPlugInConfiguration() {
	}

	@Override
	public String getFilePath() {
		return _configurationFilePath;
	}

	private void setFilePath(String fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a LibVirt Plug-In " + "Configuration file path).");
		}
		_configurationFilePath = fp;
	}

	@Override
	public void load(PropertySet ps) throws LibVirtPlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		setFilePath(ps.getSourceFile());

		// load and validate each configuration directives
		loadEndpointEnabled(ps);
		loadEndpointSecured(ps);
		loadEndpointListenIp(ps);
		loadEndpointListenPort(ps);
		loadEndpointContextRoot(ps);

		validate();
	}

	private void loadEndpointEnabled(PropertySet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_ENABLED)) {
			return;
		}
		try {
			setEndpointEnabled(ps.get(ENDPOINT_ENABLED));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_ENABLED), Ex);
		}
	}

	private void loadEndpointSecured(PropertySet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_SECURED)) {
			return;
		}
		try {
			setEndpointSecured(ps.get(ENDPOINT_SECURED));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_SECURED), Ex);
		}
	}

	private void loadEndpointListenIp(PropertySet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_LISTEN_IP)) {
			return;
		}
		try {
			setEndpointListenIp(ps.get(ENDPOINT_LISTEN_IP));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_LISTEN_IP), Ex);
		}
	}

	private void loadEndpointListenPort(PropertySet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_LISTEN_PORT)) {
			return;
		}
		try {
			setEndpointListenPort(ps.get(ENDPOINT_LISTEN_PORT));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_LISTEN_PORT),
					Ex);
		}
	}

	private void loadEndpointContextRoot(PropertySet ps)
			throws LibVirtPlugInConfigurationException {
		if (!ps.containsKey(ENDPOINT_CONTEXT_ROOT)) {
			return;
		}
		try {
			setEndpointContextRoot(ps.get(ENDPOINT_CONTEXT_ROOT));
		} catch (LibVirtPlugInConfigurationException Ex) {
			throw new LibVirtPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ENDPOINT_CONTEXT_ROOT),
					Ex);
		}
	}

	private void validate() throws LibVirtPlugInConfigurationException {
		// validate all configuration directives
		// almost nothing to

		// Start the LibVirtCloudServicesEndpoint
		if (getEndpointEnabled() == true) {
			LibVirtCloudServicesEndpoint.getInstance().start(
					getEndpointSecured(), getEndpointListenIp(),
					getEndpointListenPort(), getEndpointContextRoot());
		}
	}

	public boolean getEndpointEnabled() {
		return _endpointEnabled;
	}

	public boolean setEndpointEnabled(boolean enabled) {
		boolean previous = getEndpointEnabled();
		_endpointEnabled = enabled;
		return previous;
	}

	public boolean setEndpointEnabled(String enabled)
			throws LibVirtPlugInConfigurationException {
		try {
			return setEndpointEnabled(Bool.parseString(enabled));
		} catch (IllegalBooleanException Ex) {
			throw new LibVirtPlugInConfigurationException(Ex);
		}
	}

	public boolean getEndpointSecured() {
		return _endpointSecured;
	}

	public boolean setEndpointSecured(boolean secured) {
		boolean previous = getEndpointSecured();
		_endpointSecured = secured;
		return previous;
	}

	public boolean setEndpointSecured(String secured)
			throws LibVirtPlugInConfigurationException {
		try {
			return setEndpointSecured(Bool.parseString(secured));
		} catch (IllegalBooleanException Ex) {
			throw new LibVirtPlugInConfigurationException(Ex);
		}
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

	/**
	 * @param region
	 *            is the requested region.
	 * 
	 * @return a {@link Connect} object connected to the requested region, or
	 *         <tt>null</tt> if the requested region is not valid.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given region is <tt>null</tt>.
	 */
	public Connect getCloudConnection(String region) {
		/*
		 * TODO :this should implements ConnectAuth, to specify credentials.
		 */
		return LibVirtPooledConnection.getCloudConnection(region, null);
	}

}