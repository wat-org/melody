package com.wat.melody.cloud.network.activation;

import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkActivationDatas {

	/**
	 * Default timeout for Network Activation operations.
	 */
	public static final NetworkActivationTimeout DEFAULT_ACTIVATION_TIMEOUT = createEnableTimeout(180000);

	protected static NetworkActivationTimeout createEnableTimeout(long timeout) {
		try {
			return new NetworkActivationTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a NetworkActivationTimeout with value '" + timeout
					+ "'. " + "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private boolean _isActivationEnabled;
	private Host _host;
	private Port _port;
	private NetworkDeviceName _networkDeviceName;
	private NetworkActivationTimeout _activationTimeout;

	public NetworkActivationDatas(boolean activationEnabled,
			NetworkActivationTimeout activationTimeout,
			NetworkDeviceName devname, Host host, Port port) {
		setIsActivationEnabled(activationEnabled);
		setactivationTimeout(activationTimeout);
		setNetworkDeviceName(devname);
		setHost(host);
		setPort(port);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("activation-enabled:");
		str.append(isActivationEnabled());
		str.append(", activation-protocol:");
		str.append(getNetworkActivationProtocol());
		str.append(", activation-device-name:");
		str.append(getNetworkDeviceName());
		str.append(", activation-host:");
		str.append(getHost());
		str.append(", activation-port:");
		str.append(getPort());
		str.append(", activation-timeout:");
		str.append(getActivationTimeout());
		return str.toString();
	}

	abstract public NetworkActivationProtocol getNetworkActivationProtocol();

	public boolean isActivationEnabled() {
		return _isActivationEnabled;
	}

	private boolean setIsActivationEnabled(boolean b) {
		boolean previous = isActivationEnabled();
		_isActivationEnabled = b;
		return previous;
	}

	public Host getHost() {
		return _host;
	}

	private Host setHost(Host h) {
		if (h == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getHost();
		_host = h;
		return previous;
	}

	public Port getPort() {
		return _port;
	}

	private Port setPort(Port p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getPort();
		_port = p;
		return previous;
	}

	public NetworkDeviceName getNetworkDeviceName() {
		return _networkDeviceName;
	}

	private NetworkDeviceName setNetworkDeviceName(NetworkDeviceName netdev) {
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		NetworkDeviceName previous = getNetworkDeviceName();
		_networkDeviceName = netdev;
		return previous;
	}

	public NetworkActivationTimeout getActivationTimeout() {
		return _activationTimeout;
	}

	private NetworkActivationTimeout setactivationTimeout(
			NetworkActivationTimeout timeout) {
		NetworkActivationTimeout previous = getActivationTimeout();
		_activationTimeout = timeout;
		return previous;
	}

}
