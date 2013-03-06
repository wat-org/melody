package com.wat.melody.cloud.network;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ManagementNetworkDatas {

	/**
	 * the default timeout for management enablement operation.
	 */
	public static final ManagementNetworkEnableTimeout DEFAULT_ENABLE_TIMEOUT = createEnableTimeout(180000);

	protected static ManagementNetworkEnableTimeout createEnableTimeout(
			long iTimeout) {
		try {
			return new ManagementNetworkEnableTimeout(iTimeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ManagementNetworkEnableTimeout with value '"
					+ iTimeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private boolean mbIsManagementEnabled;
	private Host moHost;
	private Port moPort;
	private NetworkDeviceName moNetworkDeviceName;
	private ManagementNetworkEnableTimeout miEnablementTimeout;

	public ManagementNetworkDatas(boolean enable,
			ManagementNetworkEnableTimeout enableTimeout,
			NetworkDeviceName netdev, Host host, Port port) {
		setIsManagementEnabled(enable);
		setEnablementTimeout(enableTimeout);
		setNetworkDeviceName(netdev);
		setHost(host);
		setPort(port);
	}

	@Override
	public String toString() {
		return "method:" + getManagementNetworkMethod() + ", enable:"
				+ isManagementEnabled() + ", device:" + getNetworkDeviceName()
				+ ", host:" + getHost() + ", port:" + getPort() + ", timeout:"
				+ getEnablementTimeout();
	}

	abstract public ManagementNetworkMethod getManagementNetworkMethod();

	public boolean isManagementEnabled() {
		return mbIsManagementEnabled;
	}

	private boolean setIsManagementEnabled(boolean b) {
		boolean previous = isManagementEnabled();
		mbIsManagementEnabled = b;
		return previous;
	}

	public Host getHost() {
		return moHost;
	}

	private Host setHost(Host h) {
		if (h == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getHost();
		moHost = h;
		return previous;
	}

	public Port getPort() {
		return moPort;
	}

	private Port setPort(Port p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getPort();
		moPort = p;
		return previous;
	}

	public NetworkDeviceName getNetworkDeviceName() {
		return moNetworkDeviceName;
	}

	private NetworkDeviceName setNetworkDeviceName(NetworkDeviceName netdev) {
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		NetworkDeviceName previous = getNetworkDeviceName();
		moNetworkDeviceName = netdev;
		return previous;
	}

	public ManagementNetworkEnableTimeout getEnablementTimeout() {
		return miEnablementTimeout;
	}

	private ManagementNetworkEnableTimeout setEnablementTimeout(
			ManagementNetworkEnableTimeout timeout) {
		ManagementNetworkEnableTimeout previous = getEnablementTimeout();
		miEnablementTimeout = timeout;
		return previous;
	}

}
