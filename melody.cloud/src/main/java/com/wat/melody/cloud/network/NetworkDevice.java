package com.wat.melody.cloud.network;

import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDevice {

	private static GenericTimeout createTimeout(int timeout) {
		try {
			return GenericTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenericTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static GenericTimeout DEFAULT_TIMEOUT = createTimeout(90000);

	private NetworkDeviceName _devname;
	private String _mac = null;
	private String _ip = null;
	private String _fqdn = null;
	private String _ipNat = null;
	private String _fqdnNat = null;
	private GenericTimeout _attachTimeout;
	private GenericTimeout _detachTimeout;

	public NetworkDevice(NetworkDeviceName devname, String mac, String ip,
			String fqdn, String natip, String natfqdn,
			GenericTimeout attachTimeout, GenericTimeout detachTimeout) {
		setNetworkDeviceName(devname);
		setMac(mac);
		setIp(ip);
		setFQDN(fqdn);
		setNatIp(natip);
		setNatFQDN(natfqdn);
		setAttachTimeout(attachTimeout);
		setDetachTimeout(detachTimeout);
	}

	@Override
	public int hashCode() {
		return getNetworkDeviceName().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("device-name:");
		str.append(getNetworkDeviceName());
		str.append(", mac:");
		str.append(getMac());
		str.append(", ip:");
		str.append(getIP());
		str.append(", fqdn:");
		str.append(getFQDN());
		str.append(", nat-ip:");
		str.append(getNatIP());
		str.append(", nat-fqdn:");
		str.append(getNatFQDN());
		str.append(" }");
		return str.toString();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof NetworkDevice) {
			NetworkDevice nd = (NetworkDevice) anObject;
			return getNetworkDeviceName().equals(nd.getNetworkDeviceName());
		}
		return false;
	}

	public NetworkDeviceName getNetworkDeviceName() {
		return _devname;
	}

	private NetworkDeviceName setNetworkDeviceName(NetworkDeviceName devname) {
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + " .");
		}
		NetworkDeviceName previous = getNetworkDeviceName();
		_devname = devname;
		return previous;
	}

	public String getMac() {
		return _mac;
	}

	private String setMac(String mac) {
		// can be null
		String previous = getMac();
		_mac = mac;
		return previous;
	}

	public String getIP() {
		return _ip;
	}

	private String setIp(String ip) {
		// can be null
		String previous = getIP();
		_ip = ip;
		return previous;
	}

	public String getFQDN() {
		return _fqdn;
	}

	private String setFQDN(String fqdn) {
		// can be null
		String previous = getFQDN();
		_fqdn = fqdn;
		return previous;
	}

	public String getNatIP() {
		return _ipNat;
	}

	private String setNatIp(String ip) {
		// can be null
		String previous = getNatIP();
		_ipNat = ip;
		return previous;
	}

	public String getNatFQDN() {
		return _fqdnNat;
	}

	private String setNatFQDN(String fqdn) {
		// can be null
		String previous = getNatFQDN();
		_fqdnNat = fqdn;
		return previous;
	}

	public GenericTimeout getAttachTimeout() {
		return _attachTimeout;
	}

	public GenericTimeout setAttachTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		GenericTimeout previous = getAttachTimeout();
		_attachTimeout = timeout;
		return previous;
	}

	public GenericTimeout getDetachTimeout() {
		return _detachTimeout;
	}

	public GenericTimeout setDetachTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		GenericTimeout previous = getDetachTimeout();
		_detachTimeout = timeout;
		return previous;
	}

}